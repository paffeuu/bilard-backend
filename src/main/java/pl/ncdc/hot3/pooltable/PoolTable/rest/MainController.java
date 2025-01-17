package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.BallPocket;
import pl.ncdc.hot3.pooltable.PoolTable.model.ConfigurableProperties;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;


import java.lang.reflect.Field;

@RestController
@RequestMapping(path = "/pooltable")
public class MainController {

    @Autowired
    private ConfigurableProperties configurableProperties;

    @Autowired
    private Properties properties;

    @Autowired
    private TableStoryService tableStoryService;

    @Autowired
    private SimpMessagingTemplate template;

    private PoolTable table;

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

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/set-properties")
    public ResponseEntity<ConfigurableProperties> setProperties(@RequestBody ConfigurableProperties properties){
        try {
            for (Field field: ConfigurableProperties.class.getDeclaredFields())
            {
                field.setAccessible(true);
                if (!field.get(this.configurableProperties).equals(field.get(properties))) {
                    field.set(this.configurableProperties, field.get(properties));
                }
                field.setAccessible(false);
            }
        } catch (IllegalAccessException iaex) {
            iaex.printStackTrace();
        }
        return ResponseEntity.ok(this.configurableProperties);
    }

    @Scheduled(fixedRate = 125)
    public void socketSendTable() throws Exception {
        System.gc();

        this.tableStoryService
                .next()
                .findBalls();

        this.socketSendProjectorView();
        this.socketSendDynamicTable();
    }

    public void socketSendDynamicTable() throws Exception {
        table = tableStoryService
                .clone()
                .findCue()
                .makePredictions()
                .detectCollision()
                .showPrevious()
                .build();

        this.template.convertAndSend("/topic/pooltable", table);
    }

    public void socketSendProjectorView() throws Exception {
        TableStoryService tss = this.tableStoryService.clone();

        tss.setProjectorMode(true);
        tss.projectorMode();

        // Dynamic mode
        if (0 == configurableProperties.getGameMode()) {
            tss
                    .findCue()
                    .makePredictions()
                    .detectCollision()
                    .showPrevious();
        }

        table = tss.build();

        this.template.convertAndSend("/topic/projector", table);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/ball-and-pocket")
    public ResponseEntity<BallPocket> getBallPocket(@RequestBody BallPocket ballPocket) {
        System.out.println(ballPocket);
        properties.setSelectedBall(ballPocket.getBall());
        properties.setSelectedPocket(properties.transformToEnum(ballPocket.getPocket()));

        return ResponseEntity.ok(ballPocket);
    }
}
