package io.github.osvaldjr.mocks.utils.stubby;

import io.github.osvaldjr.mocks.clients.StubbyClient;
import io.github.osvaldjr.mocks.objects.StubbyRequest;
import io.github.osvaldjr.mocks.utils.stubby.assemblers.StubbyRequestAssembler;
import io.github.osvaldjr.mocks.utils.Mock;
import io.github.osvaldjr.mocks.utils.mockserver.MockServerMock;
import io.github.osvaldjr.mocks.utils.stubby.jsons.StubbyJsonRequest;
import io.github.osvaldjr.mocks.utils.stubby.jsons.StubbyJsonResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnMissingBean(MockServerMock.class)
public class StubbyMock implements Mock {

  private StubbyClient stubbyClient;
  private StubbyRequestAssembler stubbyRequestAssembler;

  @Autowired
  public StubbyMock(StubbyClient stubbyClient, StubbyRequestAssembler stubbyRequestAssembler) {
    this.stubbyClient = stubbyClient;
    this.stubbyRequestAssembler = stubbyRequestAssembler;
  }

  private static String getStubbyId(ResponseEntity response) {
    String location = response.getHeaders().getFirst("location");
    Matcher matcher = Pattern.compile(".*?\\/(\\d+)").matcher(location);
    return matcher.find() ? matcher.group(1) : null;
  }

  @Override
  public String createStubbyRequest(
      StubbyRequest.RequestBody request, StubbyRequest.ResponseBody response) {
    StubbyJsonRequest stubbyJsonRequest = stubbyRequestAssembler.assemble(request, response);
    return getStubbyId(stubbyClient.create(stubbyJsonRequest));
  }

  @Override
  public void deleteAllServices() {
    List<StubbyJsonResponse> allServices = stubbyClient.getAllServices();
    if (CollectionUtils.isNotEmpty(allServices)) {
      allServices.forEach(service -> stubbyClient.delete(service.getId()));
    }
  }

  @Override
  public Integer getMockHits(Object id) {
    return stubbyClient.getService(Integer.valueOf(id.toString())).getHits();
  }
}