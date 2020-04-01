package org.linlinjava.litemall.db.service;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallCouponMapper;
import org.linlinjava.litemall.db.dao.LitemallCouponUserMapper;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.domain.LitemallCoupon.Column;
import org.linlinjava.litemall.db.util.CouponConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 优惠卷表业务
 */
@Service
public class LitemallCouponService {
    @Resource
    private LitemallCouponMapper couponMapper;
    @Resource
    private LitemallCouponUserMapper couponUserMapper;
    //显示的字段：id，name,desc,tag,discount,min
    private Column[] result = new Column[]{Column.id, Column.name, Column.desc, Column.tag,
            Column.days, Column.startTime, Column.endTime,
            Column.discount, Column.min};

    /**
     * 查询，空参数即查询所有优惠券信息
     *
     * @param offset 第几页
     * @param limit  每页几条
     * @param sort   排序字段
     * @param order  排序规则
     * @return
     */
    public List<LitemallCoupon> queryList(int offset, int limit, String sort, String order) {
        return queryList(LitemallCouponExample.newAndCreateCriteria(), offset, limit, sort, order);
    }

    /**
     * 查询优惠券基本信息表
     * 根据优惠券赠送类型，优惠券状态，是否逻辑删除，设置排序字段即规则，设置分页查询
     *
     * @param criteria 可扩展的条件
     * @param offset   第几页
     * @param limit    每页几条
     * @param sort     排序字段
     * @param order    排序规则
     * @return
     */
    public List<LitemallCoupon> queryList(LitemallCouponExample.Criteria criteria, int offset, int limit, String sort, String order) {
        //类型TYPE_COMMON=0，状态STATUS_NORMAL=0，非逻辑删除
//        优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；
//        优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。
        criteria.andTypeEqualTo(CouponConstant.TYPE_COMMON).andStatusEqualTo(CouponConstant.STATUS_NORMAL).andDeletedEqualTo(false);
        criteria.example().setOrderByClause(sort + " " + order);
        PageHelper.startPage(offset, limit);
        return couponMapper.selectByExampleSelective(criteria.example(), result);
    }

    /**
     * @param userId 用户ID
     * @param offset 第几页
     * @param limit  每页几条
     * @return 优惠券列表
     */
    public List<LitemallCoupon> queryAvailableList(Integer userId, int offset, int limit) {
        assert userId != null; //判断userId是否存在
        // 过滤掉登录账号已经领取过的coupon
        LitemallCouponExample.Criteria c = LitemallCouponExample.newAndCreateCriteria();
        //在优惠券用户表根据用户ID找优惠券用户列表
        List<LitemallCouponUser> used = couponUserMapper.selectByExample(
                LitemallCouponUserExample.newAndCreateCriteria().andUserIdEqualTo(userId).example()
        );
        //used非空切有值
        if (used != null && !used.isEmpty()) {
            //
            c.andIdNotIn(used.stream().map(LitemallCouponUser::getCouponId).collect(Collectors.toList()));
        }
        //可扩展的条件，第几页，每页几条，排序字段，排序规则
        return queryList(c, offset, limit, "add_time", "desc");
    }

    /**
     * 分页和根据创建时间降序
     *
     * @param offset 第几页
     * @param limit  每页几条
     * @return
     */
    public List<LitemallCoupon> queryList(int offset, int limit) {
        return queryList(offset, limit, "add_time", "desc");
    }

    /**
     * @param id 优惠券ID
     * @return 优惠卷信息
     */
    public LitemallCoupon findById(Integer id) {
        return couponMapper.selectByPrimaryKey(id);
    }

    /**
     * @param code
     * @return 返回优惠券码兑换的可用优惠券
     */
    public LitemallCoupon findByCode(String code) {
        LitemallCouponExample example = new LitemallCouponExample();
        //优惠券兑换码,类型TYPE_CODE=2优惠码兑换券，STATUS_NORMAL=0
        example.or().andCodeEqualTo(code).andTypeEqualTo(CouponConstant.TYPE_CODE).andStatusEqualTo(CouponConstant.STATUS_NORMAL).andDeletedEqualTo(false);
        List<LitemallCoupon> couponList = couponMapper.selectByExample(example);
        if (couponList.size() > 1) {
            throw new RuntimeException("一个码只能兑换一张优惠券");
        } else if (couponList.size() == 0) {
            return null;
        } else {
            return couponList.get(0);
        }
    }

    /**
     * 查询新用户注册优惠券
     *
     * @return 可用的注册券
     */
    public List<LitemallCoupon> queryRegister() {
        LitemallCouponExample example = new LitemallCouponExample();
        //类型TYPE_REGISTER=1注册券，状态STATUS_NORMAL=0可用
        example.or().andTypeEqualTo(CouponConstant.TYPE_REGISTER).andStatusEqualTo(CouponConstant.STATUS_NORMAL).andDeletedEqualTo(false);
        return couponMapper.selectByExample(example);
    }

    /**
     * @param name   优惠券名称
     * @param type   优惠券券赠送类型
     * @param status 优惠券状态
     * @param page   第几页
     * @param limit  每页几条
     * @param sort   排序字段
     * @param order  排序规则
     * @return
     */
    public List<LitemallCoupon> querySelective(String name, Short type, Short status, Integer page, Integer limit, String sort, String order) {
        LitemallCouponExample example = new LitemallCouponExample();
        LitemallCouponExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        if (type != null) {
            criteria.andTypeEqualTo(type);
        }
        if (status != null) {
            criteria.andStatusEqualTo(status);
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return couponMapper.selectByExample(example);
    }

    /**
     * @param coupon 优惠券信息
     */
    public void add(LitemallCoupon coupon) {
        coupon.setAddTime(LocalDateTime.now());//设置添加优惠券时间
        coupon.setUpdateTime(LocalDateTime.now());//设置修改优惠券时间
        couponMapper.insertSelective(coupon);
    }

    /**
     * 更新优惠券修稿时间
     *
     * @param coupon 优惠券信息
     * @return
     */
    public int updateById(LitemallCoupon coupon) {
        coupon.setUpdateTime(LocalDateTime.now());
        return couponMapper.updateByPrimaryKeySelective(coupon);
    }

    /**
     * 逻辑删除优惠券信息即修改delete字段
     *
     * @param id 优惠券ID
     */
    public void deleteById(Integer id) {
        couponMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * 优惠券码
     *
     * @param num 8
     * @return
     */
    private String getRandomNum(Integer num) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        base += "0123456789";

        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 生成优惠码
     *
     * @return 可使用优惠码
     */
    public String generateCode() {
        String code = getRandomNum(8);
//        findByCode(code)==null说明此优惠券码已经用过了
        while (findByCode(code) != null) {
            code = getRandomNum(8);
        }
        return code;
    }

    /**
     * 查询过期的优惠券:即优惠券的结束时间小于现在的时间
     * 注意：如果timeType=0, 即基于领取时间有效期的优惠券，则优惠券不会过期
     *
     * @return
     */
    public List<LitemallCoupon> queryExpired() {
        LitemallCouponExample example = new LitemallCouponExample();
        //STATUS_NORMAL=0可用，TIME_TYPE_TIME=1有效时间限制，如果是0，则基于领取时间的有效天数days；如果是1，则start_time和end_time是优惠券有效期；
        example.or().andStatusEqualTo(CouponConstant.STATUS_NORMAL).andTimeTypeEqualTo(CouponConstant.TIME_TYPE_TIME).andEndTimeLessThan(LocalDateTime.now()).andDeletedEqualTo(false);
        return couponMapper.selectByExample(example);
    }
}
