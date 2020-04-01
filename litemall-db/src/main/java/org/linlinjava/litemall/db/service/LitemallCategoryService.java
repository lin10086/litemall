package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallCategoryMapper;
import org.linlinjava.litemall.db.domain.LitemallCategory;
import org.linlinjava.litemall.db.domain.LitemallCategoryExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类目表业务
 */
@Service
public class LitemallCategoryService {
    @Resource
    private LitemallCategoryMapper categoryMapper;
    //显示的字段：id,name,iconUrl,
    private LitemallCategory.Column[] CHANNEL = {LitemallCategory.Column.id, LitemallCategory.Column.name, LitemallCategory.Column.iconUrl};

    /**
     * 分类类目L1名称非推荐费逻辑删除
     *
     * @param offset 第几页
     * @param limit  每页几条
     * @return 分类类目列表并分页
     */
    public List<LitemallCategory> queryL1WithoutRecommend(int offset, int limit) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        //level是L1,名称非推荐，非逻辑删除
        example.or().andLevelEqualTo("L1").andNameNotEqualTo("推荐").andDeletedEqualTo(false);
        PageHelper.startPage(offset, limit);
        return categoryMapper.selectByExample(example);
    }

    /**
     * @param offset 第几页
     * @param limit  每页几条
     * @return 分类类目L1列表并分页
     */
    public List<LitemallCategory> queryL1(int offset, int limit) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        PageHelper.startPage(offset, limit);
        return categoryMapper.selectByExample(example);
    }

    /**
     * @return 分类类目L1列表未分页
     */
    public List<LitemallCategory> queryL1() {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        return categoryMapper.selectByExample(example);
    }

    /**
     * @param pid 分类类目L1表ID
     * @return 此分类类目下的类目列表（子类目）
     */
    public List<LitemallCategory> queryByPid(Integer pid) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        //父类目ID和是否逻辑删除
        example.or().andPidEqualTo(pid).andDeletedEqualTo(false);
        return categoryMapper.selectByExample(example);
    }

    /**
     * @param ids 类目ID集合
     * @return 类目ID在IDS内且是子类目的类目集合
     */
    public List<LitemallCategory> queryL2ByIds(List<Integer> ids) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andIdIn(ids).andLevelEqualTo("L2").andDeletedEqualTo(false);
        return categoryMapper.selectByExample(example);
    }

    /**
     * @param id 类目ID
     * @return 类目信息
     */
    public LitemallCategory findById(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    /**
     * @param id    类目ID
     * @param name  类目名称
     * @param page  第几页
     * @param size  每页几条
     * @param sort  排序字段
     * @param order 排序规则
     * @return
     */
    public List<LitemallCategory> querySelective(String id, String name, Integer page, Integer size, String sort, String order) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        LitemallCategoryExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return categoryMapper.selectByExample(example);
    }

    /**
     * 更新类目修改时间
     *
     * @param category 类目信息
     * @return
     */
    public int updateById(LitemallCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        return categoryMapper.updateByPrimaryKeySelective(category);
    }

    /**
     * 逻辑删除类目即修该delete字段
     *
     * @param id 类目ID
     */
    public void deleteById(Integer id) {
        categoryMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * 插入一条类目信息
     *
     * @param category 类目信息
     */
    public void add(LitemallCategory category) {
        category.setAddTime(LocalDateTime.now());//设置添加类目时间
        category.setUpdateTime(LocalDateTime.now());//设置修改类目时间
        categoryMapper.insertSelective(category);
    }

    /**
     * 根据L1分类类目列表和是否逻辑删除
     *
     * @return L1分类类目列表
     */
    public List<LitemallCategory> queryChannel() {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        return categoryMapper.selectByExampleSelective(example, CHANNEL);
    }
}
