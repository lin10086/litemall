package org.linlinjava.litemall.db.service;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallGoodsMapper;
import org.linlinjava.litemall.db.dao.LitemallGrouponRulesMapper;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallGrouponRules;
import org.linlinjava.litemall.db.domain.LitemallGrouponRulesExample;
import org.linlinjava.litemall.db.util.GrouponConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 团购规则表业务
 */
@Service
public class LitemallGrouponRulesService {
    @Resource
    private LitemallGrouponRulesMapper mapper;
    @Resource
    private LitemallGoodsMapper goodsMapper;
    //要显示的字段：ID，name,brief,picUrl,counterPrice,retailPrice
    private LitemallGoods.Column[] goodsColumns = new LitemallGoods.Column[]{LitemallGoods.Column.id, LitemallGoods.Column.name, LitemallGoods.Column.brief, LitemallGoods.Column.picUrl, LitemallGoods.Column.counterPrice, LitemallGoods.Column.retailPrice};

    public int createRules(LitemallGrouponRules rules) {
        rules.setAddTime(LocalDateTime.now());
        rules.setUpdateTime(LocalDateTime.now());
        return mapper.insertSelective(rules);
    }

    /**
     * 根据ID查找对应团购项
     *
     * @param id
     * @return
     */
    public LitemallGrouponRules findById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * 查询某个商品关联的团购规则
     *
     * @param goodsId
     * @return
     */
    public List<LitemallGrouponRules> queryByGoodsId(Integer goodsId) {
        LitemallGrouponRulesExample example = new LitemallGrouponRulesExample();
        example.or().andGoodsIdEqualTo(goodsId).andStatusEqualTo(GrouponConstant.RULE_STATUS_ON).andDeletedEqualTo(false);
        return mapper.selectByExample(example);
    }

    public int countByGoodsId(Integer goodsId) {
        LitemallGrouponRulesExample example = new LitemallGrouponRulesExample();
        example.or().andGoodsIdEqualTo(goodsId).andStatusEqualTo(GrouponConstant.RULE_STATUS_ON).andDeletedEqualTo(false);
        return (int) mapper.countByExample(example);
    }

    public List<LitemallGrouponRules> queryByStatus(Short status) {
        LitemallGrouponRulesExample example = new LitemallGrouponRulesExample();
        example.or().andStatusEqualTo(status).andDeletedEqualTo(false);
        return mapper.selectByExample(example);
    }

    /**
     * 获取首页团购规则列表
     *
     * @param page  第几页
     * @param limit 排序规则
     * @return 团购规则列表
     */
    public List<LitemallGrouponRules> queryList(Integer page, Integer limit) {
        return queryList(page, limit, "add_time", "desc");
    }

    /**
     * @param page  第几页
     * @param limit 每页几条
     * @param sort  排序字段
     * @param order 排序规则
     * @return 团购规则列表
     */
    public List<LitemallGrouponRules> queryList(Integer page, Integer limit, String sort, String order) {
        LitemallGrouponRulesExample example = new LitemallGrouponRulesExample();
        //团购规则状态RULE_STATUS_ON=0正常上线，是否逻辑删除
        example.or().andStatusEqualTo(GrouponConstant.RULE_STATUS_ON).andDeletedEqualTo(false);
//        设置排序字段和排序规则
        example.setOrderByClause(sort + " " + order);
        PageHelper.startPage(page, limit);
        return mapper.selectByExample(example);
    }

    /**
     * 判断某个团购规则是否已经过期
     *
     * @return
     */
    public boolean isExpired(LitemallGrouponRules rules) {
        return (rules == null || rules.getExpireTime().isBefore(LocalDateTime.now()));
    }

    /**
     * 获取团购规则列表
     *
     * @param goodsId
     * @param page
     * @param size
     * @param sort
     * @param order
     * @return
     */
    public List<LitemallGrouponRules> querySelective(String goodsId, Integer page, Integer size, String sort, String order) {
        LitemallGrouponRulesExample example = new LitemallGrouponRulesExample();
        example.setOrderByClause(sort + " " + order);

        LitemallGrouponRulesExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(goodsId)) {
            criteria.andGoodsIdEqualTo(Integer.parseInt(goodsId));
        }
        criteria.andDeletedEqualTo(false);

        PageHelper.startPage(page, size);
        return mapper.selectByExample(example);
    }

    public void delete(Integer id) {
        mapper.logicalDeleteByPrimaryKey(id);
    }

    public int updateById(LitemallGrouponRules grouponRules) {
        grouponRules.setUpdateTime(LocalDateTime.now());
        return mapper.updateByPrimaryKeySelective(grouponRules);
    }
}