package org.linlinjava.litemall.wx.service;

import com.github.pagehelper.Page;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallGrouponRules;
import org.linlinjava.litemall.db.service.LitemallGoodsService;
import org.linlinjava.litemall.db.service.LitemallGrouponRulesService;
import org.linlinjava.litemall.db.service.LitemallGrouponService;
import org.linlinjava.litemall.wx.vo.GrouponRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 团购VO类业务
 */
@Service
public class WxGrouponRuleService {
    private final Log logger = LogFactory.getLog(WxGrouponRuleService.class);
    /**
     * 团购规则表业务
     */
    @Autowired
    private LitemallGrouponRulesService grouponRulesService;
    @Autowired
    private LitemallGrouponService grouponService;
    /**
     * 商品表业务
     */
    @Autowired
    private LitemallGoodsService goodsService;

    /**
     * @param page 第几页
     * @param size 每一位几条
     * @return
     */
    public List<GrouponRuleVo> queryList(Integer page, Integer size) {
        return queryList(page, size, "add_time", "desc");
    }


    /**
     * 获取团购表信息
     *
     * @param page  第几页
     * @param size  每页大小
     * @param sort  排序字段
     * @param order 排序规则
     * @return
     */
    public List<GrouponRuleVo> queryList(Integer page, Integer size, String sort, String order) {
        //获取团购规则列表，并分页，团购规则状态RULE_STATUS_ON=0正常上线，是否逻辑删除
        Page<LitemallGrouponRules> grouponRulesList = (Page) grouponRulesService.queryList(page, size, sort, order);
        //团购列表
        Page<GrouponRuleVo> grouponList = new Page<GrouponRuleVo>();
//        设置团购列表分页信息
        grouponList.setPages(grouponRulesList.getPages());
        grouponList.setPageNum(grouponRulesList.getPageNum());
        grouponList.setPageSize(grouponRulesList.getPageSize());
        grouponList.setTotal(grouponRulesList.getTotal());
        //遍历团购规则列表
        for (LitemallGrouponRules rule : grouponRulesList) {
            Integer goodsId = rule.getGoodsId();//商品规则表中的商品ID
            LitemallGoods goods = goodsService.findById(goodsId);//获取商品信息
            if (goods == null)
                continue;
//            设置团购列表
            GrouponRuleVo grouponRuleVo = new GrouponRuleVo();
            grouponRuleVo.setId(goods.getId());//商品ID
            grouponRuleVo.setName(goods.getName());//商品名称
            grouponRuleVo.setBrief(goods.getBrief());//商品简介
            grouponRuleVo.setPicUrl(goods.getPicUrl());//商品页面图片
            grouponRuleVo.setCounterPrice(goods.getCounterPrice());//
            grouponRuleVo.setRetailPrice(goods.getRetailPrice());//商品的零售价格
            grouponRuleVo.setGrouponPrice(goods.getRetailPrice().subtract(rule.getDiscount()));//零售价格和优惠金额
            grouponRuleVo.setGrouponDiscount(rule.getDiscount());//优惠金额
            grouponRuleVo.setGrouponMember(rule.getDiscountMember());//达到优惠条件的人数
            grouponRuleVo.setExpireTime(rule.getExpireTime());//团购过期时间
            grouponList.add(grouponRuleVo);
        }

        return grouponList;
    }
}