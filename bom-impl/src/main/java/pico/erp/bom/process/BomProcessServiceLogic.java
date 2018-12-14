package pico.erp.bom.process;

import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.bom.BomId;
import pico.erp.process.ProcessId;
import pico.erp.shared.Public;
import pico.erp.shared.event.EventPublisher;

@Service
@Public
@Transactional
@Validated
public class BomProcessServiceLogic implements BomProcessService {

  @Autowired
  private BomProcessRepository bomProcessRepository;

  @Autowired
  private BomProcessMapper mapper;

  @Autowired
  private EventPublisher eventPublisher;

  @Override
  public void changeOrder(BomProcessRequests.ChangeOrderRequest request) {
    val bomProcess = bomProcessRepository.findBy(request.getId())
      .orElseThrow(BomProcessExceptions.NotFoundException::new);
    val response = bomProcess.apply(mapper.map(request));
    bomProcessRepository.update(bomProcess);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public BomProcessData create(BomProcessRequests.CreateRequest request) {
    if (bomProcessRepository.exists(request.getId())) {
      throw new BomProcessExceptions.AlreadyExistsException();
    }
    if (bomProcessRepository.exists(request.getBomId(), request.getProcessId())) {
      throw new BomProcessExceptions.AlreadyExistsException();
    }
    val bomProcess = new BomProcess();
    val response = bomProcess.apply(mapper.map(request));
    val created = bomProcessRepository.create(bomProcess);
    eventPublisher.publishEvents(response.getEvents());
    return mapper.map(created);
  }

  @Override
  public void delete(BomProcessRequests.DeleteRequest request) {
    val bomProcess = bomProcessRepository.findBy(request.getId())
      .orElseThrow(BomProcessExceptions.NotFoundException::new);
    val response = bomProcess.apply(mapper.map(request));
    bomProcessRepository.deleteBy(request.getId());
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public BomProcessData get(BomProcessId id) {
    return bomProcessRepository.findBy(id)
      .map(mapper::map)
      .orElseThrow(BomProcessExceptions.NotFoundException::new);
  }

  @Override
  public List<BomProcessData> getAll(BomId bomId) {
    return bomProcessRepository.findAllBy(bomId)
      .map(mapper::map)
      .collect(Collectors.toList());
  }

  @Override
  public List<BomProcessData> getAll(ProcessId processId) {
    return bomProcessRepository.findAllBy(processId)
      .map(mapper::map)
      .collect(Collectors.toList());
  }

  @Override
  public void update(BomProcessRequests.UpdateRequest request) {
    val bomProcess = bomProcessRepository.findBy(request.getId())
      .orElseThrow(BomProcessExceptions.NotFoundException::new);
    val response = bomProcess.apply(mapper.map(request));
    bomProcessRepository.update(bomProcess);
    eventPublisher.publishEvents(response.getEvents());
  }
}
