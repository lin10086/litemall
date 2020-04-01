package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallCategory;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.linlinjava.litemall.wx.service.HomeCacheManager;
import org.linlinjava.litemall.wx.service.WxGrouponRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 首页服务
 */
@RestController
@RequestMapping("/wx/home")
@Validated
public class WxHomeController {
    private final Log logger = LogFactory.getLog(WxHomeController.class);

    /**
     * 广告业务
     */
    @Autowired
    private LitemallAdService adService;
    /**
     * 商品业务
     */
    @Autowired
    private LitemallGoodsService goodsService;
    /**
     * 品牌商业务
     */
    @Autowired
    private LitemallBrandService brandService;
    /**
     * 专题表业务
     */
    @Autowired
    private LitemallTopicService topicService;
    /**
     * 类目表业务
     */
    @Autowired
    private LitemallCategoryService categoryService;
    /**
     * 团购VO类业务
     */
    @Autowired
    private WxGrouponRuleService grouponService;
    /**
     * 优惠卷表业务
     */
    @Autowired
    private LitemallCouponService couponService;

    private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

    private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 9, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);

    @GetMapping("/cache")
    public Object cache(@NotNull String key) {
        if (!key.equals("litemall_cache")) {
            return ResponseUtil.fail();
        }

        // 清除缓存
        HomeCacheManager.clearAll();
        return ResponseUtil.ok("缓存已清除");
    }

    /**
     * 首页数据
     *
     * @param userId 当用户已经登录时，非空。为登录状态为null
     * @return 首页数据
     */
    @GetMapping("/index")
    public Object index(@LoginUser Integer userId) {
        //优先从缓存中读取
        //判断缓存中是否有数据
        if (HomeCacheManager.hasData(HomeCacheManager.INDEX)) {
            return ResponseUtil.ok(HomeCacheManager.getCacheData(HomeCacheManager.INDEX));
        }
//        创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程
        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        根据广告位置1位首页，是否逻辑删除，是否启动
        Callable<List> bannerListCallable = () -> adService.queryIndex();
//        根据类目标准和是否逻辑删除
        Callable<List> channelListCallable = () -> categoryService.queryChannel();

        Callable<List> couponListCallable;
        if (userId == null) {
            //第一页，每页3条，并根据创建时间降序找优惠券表列表
            couponListCallable = () -> couponService.queryList(0, 3);
        } else {
            //先根据用户ID在用户优惠券表中找用户优惠券列表，获取其中的优惠券表ID，在获取优惠券ID不在优惠券列表中的列表信息
            couponListCallable = () -> couponService.queryAvailableList(userId, 0, 3);
        }
        //获取新品发布的产品列表 默认SystemConfig.getNewLimit()=6条每页，条件：否是新品，是否上架，是否逻辑删除
        Callable<List> newGoodsListCallable = () -> goodsService.queryByNew(0, SystemConfig.getNewLimit());
        //获取人气产品及热卖产品，默认每页SystemConfig.getHotLimit()6条，条件：是否是人气热卖产品，是否上架，是否逻辑删除
        Callable<List> hotGoodsListCallable = () -> goodsService.queryByHot(0, SystemConfig.getHotLimit());
        //获取品牌商列表并分页，每页SystemConfig.getBrandLimit()=4条，条件：是否逻辑删除
        Callable<List> brandListCallable = () -> brandService.query(0, SystemConfig.getBrandLimit());
//        获取专题商品列表并分页，每页LITEMALL_WX_INDEX_TOPIC=4条，条件：是否逻辑删除
        Callable<List> topicListCallable = () -> topicService.queryList(0, SystemConfig.getTopicLimit());

        //团购VO列表（有商品信息和商品规则信息）第一页，每页5条
        Callable<List> grouponListCallable = () -> grouponService.queryList(0, 5);

        Callable<List> floorGoodsListCallable = this::getCategoryList;
//        把任务放进FutureTask里面
        FutureTask<List> bannerTask = new FutureTask<>(bannerListCallable);
        FutureTask<List> channelTask = new FutureTask<>(channelListCallable);
        FutureTask<List> couponListTask = new FutureTask<>(couponListCallable);
        FutureTask<List> newGoodsListTask = new FutureTask<>(newGoodsListCallable);
        FutureTask<List> hotGoodsListTask = new FutureTask<>(hotGoodsListCallable);
        FutureTask<List> brandListTask = new FutureTask<>(brandListCallable);
        FutureTask<List> topicListTask = new FutureTask<>(topicListCallable);
        FutureTask<List> grouponListTask = new FutureTask<>(grouponListCallable);
        FutureTask<List> floorGoodsListTask = new FutureTask<>(floorGoodsListCallable);
//        执行任务
        executorService.submit(bannerTask);
        executorService.submit(channelTask);
        executorService.submit(couponListTask);
        executorService.submit(newGoodsListTask);
        executorService.submit(hotGoodsListTask);
        executorService.submit(brandListTask);
        executorService.submit(topicListTask);
        executorService.submit(grouponListTask);
        executorService.submit(floorGoodsListTask);
//        任务的返回结果
        Map<String, Object> entity = new HashMap<>();
        try {
            entity.put("banner", bannerTask.get());
            entity.put("channel", channelTask.get());
            entity.put("couponList", couponListTask.get());
            entity.put("newGoodsList", newGoodsListTask.get());
            entity.put("hotGoodsList", hotGoodsListTask.get());
            entity.put("brandList", brandListTask.get());
            entity.put("topicList", topicListTask.get());
            entity.put("grouponList", grouponListTask.get());
            entity.put("floorGoodsList", floorGoodsListTask.get());
            //缓存数据
            HomeCacheManager.loadData(HomeCacheManager.INDEX, entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        return ResponseUtil.ok(entity);
    }

    /**
     * 封装商品目录和商品列表
     *
     * @return
     */
    private List<Map> getCategoryList() {
        List<Map> categoryList = new ArrayList<>();

        //根据level是L1,名称非推荐，非逻辑删除，返回（分类类目列表相等于L1目录列表）并分页  SystemConfig.getCatlogListLimit()= 4
        List<LitemallCategory> catL1List = categoryService.queryL1WithoutRecommend(0, SystemConfig.getCatlogListLimit());
        //遍历L1级目录
        for (LitemallCategory catL1 : catL1List) {
            //根据分类类目ID获取分类类目下的类目(相等于L2级目录列表）
            List<LitemallCategory> catL2List = categoryService.queryByPid(catL1.getId());
            //用于存放L2级类目ID
            List<Integer> l2List = new ArrayList<>();
            for (LitemallCategory catL2 : catL2List) {
                l2List.add(catL2.getId());
            }
            //商品表列表
            List<LitemallGoods> categoryGoods;
            if (l2List.size() == 0) {
                categoryGoods = new ArrayList<>();
            } else {
//                获取L2级分类下的商品并分页，SystemConfig.getCatlogMoreLimit()=4
                categoryGoods = goodsService.queryByCategory(l2List, 0, SystemConfig.getCatlogMoreLimit());
            }

            Map<String, Object> catGoods = new HashMap<>();
            catGoods.put("id", catL1.getId());//L1级目录ID
            catGoods.put("name", catL1.getName());//L1级目录名称
            catGoods.put("goodsList", categoryGoods);//L2级目录下的商品表列表
            categoryList.add(catGoods);
        }
        return categoryList;
    }

    /**
     * 商城介绍信息
     *
     * @return 商城介绍信息
     */
    @GetMapping("/about")
    public Object about() {
        Map<String, Object> about = new HashMap<>();
        about.put("name", SystemConfig.getMallName());//litemall
        about.put("address", SystemConfig.getMallAddress());//上海
        about.put("phone", SystemConfig.getMallPhone());//021-xxxx-xxxx
        about.put("qq", SystemConfig.getMallQQ());//705144434
        about.put("longitude", SystemConfig.getMallLongitude());//121.587839   经度
        about.put("latitude", SystemConfig.getMallLatitude());//31.201900  纬度
        return ResponseUtil.ok(about);
    }
}