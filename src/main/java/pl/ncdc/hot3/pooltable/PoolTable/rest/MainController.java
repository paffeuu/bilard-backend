package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;

@RestController
@RequestMapping(path = "/pooltable")
public class MainController {

    @Autowired
    private Properties properties;

    @Autowired
    private TableStoryService tableStoryService;

    @Autowired
    private SimpMessagingTemplate template;


    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/get-pool-table")
    public ResponseEntity<PoolTable> test() throws CameraServiceException {

        PoolTable table = tableStoryService
                .next()
                .findBalls()
                .findCue()
                .makePredictions()
                .detectCollision()
                .showPrevious()
                .build();
        return ResponseEntity.ok(table);

    }

    @PutMapping("/get-pool-table")
    public ResponseEntity<Properties> setProperties(@RequestParam Properties properties){
//        Mapper mapper = new DozerBeanMapper();
        return ResponseEntity.ok(this.properties);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/set-visible", method = RequestMethod.GET)
    public void setShowPrevious() {
        properties.setShowPreviousPosition(!properties.isShowPreviousPosition());
    }

    @Scheduled(fixedRate = 125)
    public void socketSendTable() throws Exception{
        System.gc();
        PoolTable table = tableStoryService
                .next()
                .findBalls()
                .findCue()
                .makePredictions()
                .detectCollision()
                .showPrevious()
                .build();

        this.template.convertAndSend("/topic/pooltable", table);
    }

}
