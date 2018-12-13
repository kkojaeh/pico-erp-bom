package pico.erp.bom.process;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface BomProcessExceptions {


  @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "bom-process.already.exists.exception")
  class AlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }

  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "bom-process.not.found.exception")
  class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

  }

  @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "bom-process.cannot.change.order.exception")
  class CannotChangeOrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }
}
