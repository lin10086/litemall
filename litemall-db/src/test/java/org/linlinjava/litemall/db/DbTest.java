package org.linlinjava.litemall.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.linlinjava.litemall.db.dao.LitemallAdMapper;
import org.linlinjava.litemall.db.dao.LitemallCategoryMapper;
import org.linlinjava.litemall.db.domain.LitemallAd;
import org.linlinjava.litemall.db.domain.LitemallAdExample;
import org.linlinjava.litemall.db.domain.LitemallCategory;
import org.linlinjava.litemall.db.domain.LitemallCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import javax.annotation.Resource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTest {
    @Resource
    private LitemallAdMapper adMapper;
    @Resource
    private LitemallCategoryMapper categoryMapper;

    private LitemallCategory.Column[] CHANNEL = {LitemallCategory.Column.id, LitemallCategory.Column.name, LitemallCategory.Column.iconUrl};

    @Test
    public void testOr() {
        LitemallAdExample example = new LitemallAdExample();
        example.or().andPositionEqualTo((byte) 1).andDeletedEqualTo(false).andEnabledEqualTo(true);
        List<LitemallAd> litemallAds = adMapper.selectByExample(example);

    }

    @Test
    public void testAnd() {
        LitemallAdExample example = new LitemallAdExample();
        example.or().andPositionEqualTo((byte) 1).andDeletedEqualTo(false).andEnabledEqualTo(true);
        List<LitemallAd> litemallAds = adMapper.selectByExample(example);
    }

    @Test
    public void queryChannel() {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        List<LitemallCategory> litemallCategories = categoryMapper.selectByExampleSelective(example);
//        log.info("litemallCategories={}", litemallCategories);
        for (LitemallCategory l:litemallCategories) {
            System.err.println(l);
        }
    }

}
