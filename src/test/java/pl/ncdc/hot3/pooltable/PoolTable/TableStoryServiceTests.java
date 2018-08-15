package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TableStoryService.class, Detector.class})
public class TableStoryServiceTests {

//    @Autowired
//    private TableStoryService tableStoryService;
//
//    @Test
//    public void shouldReturnPoolTableModelWithAllDetails(){
//
//        PoolTable table = tableStoryService
//                .next()
//                .findCue()
//                .makePredictions()
//                .findBalls()
//                .showPrevious()
//                .build();
//
//        Assert.assertNotNull(table);
//    }

}
