package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallTopicMapper;
import org.linlinjava.litemall.db.domain.LitemallGroupon;
import org.linlinjava.litemall.db.domain.LitemallTopic;
import org.linlinjava.litemall.db.domain.LitemallTopic.Column;
import org.linlinjava.litemall.db.domain.LitemallTopicExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 专题表业务
 */
@Service
public class LitemallTopicService {
    @Resource
    private LitemallTopicMapper topicMapper;
    //显示字段：id,title,subtitle,price,picUrl,readCount
    private Column[] columns = new Column[]{Column.id, Column.title, Column.subtitle, Column.price, Column.picUrl, Column.readCount};

    /**
     * @param offset 第几页
     * @param limit  每页几条
     * @return
     */
    public List<LitemallTopic> queryList(int offset, int limit) {
        return queryList(offset, limit, "add_time", "desc");
    }

    /**
     * @param offset 第几页
     * @param limit  每页几条
     * @param sort   排序字段
     * @param order  排序规则
     * @return 没有逻辑删除的专题列表
     */
    public List<LitemallTopic> queryList(int offset, int limit, String sort, String order) {
        LitemallTopicExample example = new LitemallTopicExample();
//        是否逻辑删除
        example.or().andDeletedEqualTo(false);
//        设置排序字段和排序规则
        example.setOrderByClause(sort + " " + order);
        PageHelper.startPage(offset, limit);
        return topicMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 非逻辑删除的标题总数
     *
     * @return
     */
    public int queryTotal() {
        LitemallTopicExample example = new LitemallTopicExample();
        example.or().andDeletedEqualTo(false);
        return (int) topicMapper.countByExample(example);
    }

    /**
     * @param id 专题ID
     * @return 返回首标题信息
     */
    public LitemallTopic findById(Integer id) {
        LitemallTopicExample example = new LitemallTopicExample();
        example.or().andIdEqualTo(id).andDeletedEqualTo(false);
        return topicMapper.selectOneByExampleWithBLOBs(example);
    }

    /**
     * 返回除了此标题（ID）外的所有标题
     *
     * @param id     标题ID
     * @param offset 第几页
     * @param limit  每页几条
     * @return
     */
    public List<LitemallTopic> queryRelatedList(Integer id, int offset, int limit) {
        LitemallTopicExample example = new LitemallTopicExample();
        example.or().andIdEqualTo(id).andDeletedEqualTo(false);
//        返回没有逻辑删除的标题信息
        List<LitemallTopic> topics = topicMapper.selectByExample(example);
        if (topics.size() == 0) {
//            没有逻辑删除的专题列表按添加标题的时间降序并分页
            return queryList(offset, limit, "add_time", "desc");
        }
        LitemallTopic topic = topics.get(0);//获取标题信息

        example = new LitemallTopicExample();
        //非此标题的所有非逻辑删除标题
        example.or().andIdNotEqualTo(topic.getId()).andDeletedEqualTo(false);
        PageHelper.startPage(offset, limit);
        List<LitemallTopic> relateds = topicMapper.selectByExampleWithBLOBs(example);
        if (relateds.size() != 0) {
            return relateds;
        }

        return queryList(offset, limit, "add_time", "desc");
    }

    /**
     * @param title    专题标题
     * @param subtitle 专题子标题
     * @param page     第几页
     * @param limit    每页几条
     * @param sort     排序字段
     * @param order    排序规则
     * @return
     */
    public List<LitemallTopic> querySelective(String title, String subtitle, Integer page, Integer limit, String sort, String order) {
        LitemallTopicExample example = new LitemallTopicExample();
        LitemallTopicExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(title)) {
            criteria.andTitleLike("%" + title + "%");
        }
        if (!StringUtils.isEmpty(subtitle)) {
            criteria.andSubtitleLike("%" + subtitle + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return topicMapper.selectByExampleWithBLOBs(example);
    }

    /**
     * @param topic 专题信息
     * @return
     */
    public int updateById(LitemallTopic topic) {
        topic.setUpdateTime(LocalDateTime.now());//更新修改时间
        LitemallTopicExample example = new LitemallTopicExample();
        example.or().andIdEqualTo(topic.getId());//更新条件
        return topicMapper.updateByExampleSelective(topic, example);
    }

    /**
     * 删除单个专题（逻辑删除即修改delete字段）
     *
     * @param id 专题ID
     */
    public void deleteById(Integer id) {
        topicMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * @param topic 专题表信息
     */
    public void add(LitemallTopic topic) {
        topic.setAddTime(LocalDateTime.now());// 设置添加专题表的时间
        topic.setUpdateTime(LocalDateTime.now());//设置修改专题表的时间
        topicMapper.insertSelective(topic);
    }


    /**
     * 删除多个专题
     *
     * @param ids 专题IDS集合
     */
    public void deleteByIds(List<Integer> ids) {
        LitemallTopicExample example = new LitemallTopicExample();
        //专题表ID集合，是否逻辑删除
        example.or().andIdIn(ids).andDeletedEqualTo(false);
        LitemallTopic topic = new LitemallTopic();
        //更新专题表修改时间
        topic.setUpdateTime(LocalDateTime.now());
        //修改delete字段
        topic.setDeleted(true);
        topicMapper.updateByExampleSelective(topic, example);
    }
}
