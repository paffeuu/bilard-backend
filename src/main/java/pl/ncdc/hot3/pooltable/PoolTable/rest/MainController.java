package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;

import java.io.File;
import java.lang.reflect.Field;

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

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/set-properties")
    public ResponseEntity<Properties> setProperties(@RequestBody Properties properties){
        try {
            for (Field field: Properties.class.getDeclaredFields())
            {
                field.setAccessible(true);
                if (!field.get(this.properties).equals(field.get(properties))) {
                    field.set(this.properties, field.get(properties));
                }
                field.setAccessible(false);
            }
        } catch (IllegalAccessException iaex) {
            iaex.printStackTrace();
        }
        return ResponseEntity.ok(this.properties);
    }

    @Scheduled(fixedRate = 125)
    public void socketSendTable() throws Exception {
        System.gc();
//        PoolTable table;
//
//        if (this.passiveMode) {
//            table = tableStoryService
//                    .next()
//                    .findBalls()
//                    .findCue()
//                    .makePredictions()
//                    .detectCollision()
//                    .showPrevious()
//                    .build();
//        } else {
//            table = tableStoryService
//                    .next()
//                    .findBalls()
//                    .passiveMode()
//                    .build();
//        }

        PoolTable table = tableStoryService
                .next()
                .findBalls()
                .build();

        this.template.convertAndSend("/topic/pooltable", table);
    }
}
