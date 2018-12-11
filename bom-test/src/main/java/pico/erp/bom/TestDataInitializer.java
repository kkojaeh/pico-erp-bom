package pico.erp.bom;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import pico.erp.bom.material.BomMaterialRequests;
import pico.erp.bom.material.BomMaterialService;
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


  @Autowired
  private DataProperties dataProperties;

  @SneakyThrows
  @Override
  public void initialize() {
    dataProperties.bomDrafts.forEach(bomService::draft);
    dataProperties.bomProcesses.forEach(bomService::update);
    dataProperties.bomMaterials.forEach(request -> {
      bomMaterialService.create(request);
      TimeUnit.MILLISECONDS.sleep(500L);
    });
    dataProperties.bomDetermines.forEach(bomService::determine);
  }

  @Data
  @Configuration
  @ConfigurationProperties("data")
  public static class DataProperties {

    List<BomRequests.DraftRequest> bomDrafts = new LinkedList<>();

    List<BomRequests.UpdateRequest> bomProcesses = new LinkedList<>();

    List<BomMaterialRequests.CreateRequest> bomMaterials = new LinkedList<>();

    List<BomRequests.DetermineRequest> bomDetermines = new LinkedList<>();

  }

}
