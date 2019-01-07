package pico.erp.bom;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import pico.erp.bom.material.BomMaterialRequests;
import pico.erp.bom.material.BomMaterialService;
import pico.erp.bom.process.BomProcessRequests;
import pico.erp.bom.process.BomProcessService;
import pico.erp.shared.ApplicationInitializer;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@Profile({"!development", "!production"})
public class TestDataInitializer implements ApplicationInitializer {

  @Lazy
  @Autowired
  private BomService bomService;

  @Lazy
  @Autowired
  private BomMaterialService bomMaterialService;

  @Lazy
  @Autowired
  private BomProcessService bomProcessService;


  @Autowired
  private DataProperties dataProperties;

  @Override
  public void initialize() {
    dataProperties.bomDrafts.forEach(bomService::draft);
    dataProperties.bomMaterials.forEach(request -> {
      bomMaterialService.create(request);
      try {
        TimeUnit.MILLISECONDS.sleep(200L);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    dataProperties.bomProcesses.forEach(bomProcessService::create);
    dataProperties.bomDetermines.forEach(bomService::determine);
  }

  @Data
  @Configuration
  @ConfigurationProperties("data")
  public static class DataProperties {

    List<BomProcessRequests.CreateRequest> bomProcesses = new LinkedList<>();

    List<BomRequests.DraftRequest> bomDrafts = new LinkedList<>();

    List<BomMaterialRequests.CreateRequest> bomMaterials = new LinkedList<>();

    List<BomRequests.DetermineRequest> bomDetermines = new LinkedList<>();

  }

}