package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallBrandMapper;
import org.linlinjava.litemall.db.domain.LitemallBrand;
import org.linlinjava.litemall.db.domain.LitemallBrand.Column;
import org.linlinjava.litemall.db.domain.LitemallBrandExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 品牌商业务
 */
@Service
public class LitemallBrandService {
    @Resource
    private LitemallBrandMapper brandMapper;
    //显示的字段id,name,desc,prcUrl,floorPrice
    private Column[] columns = new Column[]{Column.id, Column.name, Column.desc, Column.picUrl, Column.floorPrice};

    /**
     * @param page  第几页
     * @param limit 每页几条
     * @param sort  排序字段
     * @param order 排序规则
     * @return 品牌商列表
     */
    public List<LitemallBrand> query(Integer page, Integer limit, String sort, String order) {
        LitemallBrandExample example = new LitemallBrandExample();
        //是否逻辑删除
        example.or().andDeletedEqualTo(false);
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
//            设置排序字段和排序规则
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(page, limit);//分页
        return brandMapper.selectByExampleSelective(example, columns);
    }

    /**
     * @param page  第几页
     * @param limit 每页几条
     * @return 品牌商列表
     */
    public List<LitemallBrand> query(Integer page, Integer limit) {
        return query(page, limit, null, null);
    }

    /**
     * 根据品牌商ID获取品牌商信息
     *
     * @param id 品牌商ID
     * @return
     */
    public LitemallBrand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * @param id    商品ID
     * @param name  商品名称
     * @param page  第几页
     * @param size  每页几条
     * @param sort  排序字段
     * @param order 排序规则
     * @return 品牌商信息列表 并分页
     */
    public List<LitemallBrand> querySelective(String id, String name, Integer page, Integer size, String sort, String order) {
        LitemallBrandExample example = new LitemallBrandExample();
        LitemallBrandExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));//商品ID
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");//商品名称像
        }
        criteria.andDeletedEqualTo(false);//是否逻辑删除

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order); //设置排序字段即排序规则
        }

        PageHelper.startPage(page, size);
        return brandMapper.selectByExample(example);
    }

    /**
     * 更新商品的修改时间
     *
     * @param brand 品牌商信息
     * @return
     */
    public int updateById(LitemallBrand brand) {
        brand.setUpdateTime(LocalDateTime.now());
        return brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 逻辑删除即更新delete字段
     *
     * @param id 品牌商ID
     */
    public void deleteById(Integer id) {
        brandMapper.logicalDeleteByPrimaryKey(id);
    }

    /**
     * 插入一条品牌商信息
     * LocalDateTime.now()现在的时刻
     *
     * @param brand 品牌商信息
     */
    public void add(LitemallBrand brand) {
        brand.setAddTime(LocalDateTime.now());//创建时间
        brand.setUpdateTime(LocalDateTime.now());//修改时间
        brandMapper.insertSelective(brand);
    }

    /**
     * 获取所有的品牌商（逻辑删除为false）
     *
     * @return
     */
    public List<LitemallBrand> all() {
        LitemallBrandExample example = new LitemallBrandExample();
        example.or().andDeletedEqualTo(false);
        return brandMapper.selectByExample(example);
    }
}
