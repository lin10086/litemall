package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallAdMapper;
import org.linlinjava.litemall.db.domain.LitemallAd;
import org.linlinjava.litemall.db.domain.LitemallAdExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 广告业务
 */
@Service
public class LitemallAdService {
    @Resource
    private LitemallAdMapper adMapper;

    /**
     * 根据广告位置1位首页，是否逻辑删除，是否启动
     *
     * @return 返回广告列表
     */
    public List<LitemallAd> queryIndex() {
        LitemallAdExample example = new LitemallAdExample();
        example.or().andPositionEqualTo((byte) 1).andDeletedEqualTo(false).andEnabledEqualTo(true);
        return adMapper.selectByExample(example);
    }

    /**
     * @param name    广告标题模糊字
     * @param content 活动内容
     * @param page    第几页
     * @param limit   每页几条
     * @param sort    排序字段
     * @param order   排序规则
     * @return
     */
    public List<LitemallAd> querySelective(String name, String content, Integer page, Integer limit, String sort, String order) {
        LitemallAdExample example = new LitemallAdExample();
        LitemallAdExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        if (!StringUtils.isEmpty(content)) {
            criteria.andContentLike("%" + content + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return adMapper.selectByExample(example);
    }

    /**
     * 更新广告修改时间
     *
     * @param ad 广告信息
     * @return
     */
    public int updateById(LitemallAd ad) {
        ad.setUpdateTime(LocalDateTime.now());
        return adMapper.updateByPrimaryKeySelective(ad);
    }

    /**
     * 逻辑删除广告信息即修改delete字段
     *
     * @param id 广告表ID
     */
    public void deleteById(Integer id) {
        adMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * @param ad 广告信息
     */
    public void add(LitemallAd ad) {
        ad.setAddTime(LocalDateTime.now());//设置添加广告时间
        ad.setUpdateTime(LocalDateTime.now());//设置修改广告的信息
        adMapper.insertSelective(ad);
    }

    /**
     * @param id 广告表ID
     * @return 返回广告信息
     */
    public LitemallAd findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }
}
