package net.clore.wxcr.controller;

import net.clore.wxcr.config.ApiResponse;
import net.clore.wxcr.dto.CounterRequest;
import net.clore.wxcr.model.Counter;
import net.clore.wxcr.service.CounterService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/** counter控制器 */
@RestController
public class CounterController {

  final CounterService counterService;
  final Logger logger;

  public CounterController(@Autowired CounterService counterService) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
  }

  /**
   * 获取当前计数
   *
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  ApiResponse get(@RequestHeader Map<String, String> headers) {
    String openid = headers.get("x-wx-openid");
    logger.info("/api/count get request, openid: {}", openid);

    logger.info("headers：{} openid: {}", headers.size(), openid);
    headers.forEach((key, value) -> {
      logger.info(String.format("Header '%s' = %s", key, value));
    });

    openid = StringUtils.defaultIfBlank(openid, "_");
    Optional<Counter> counter = counterService.getCounter(openid);
    Integer count = 0;
    if (counter.isPresent()) {
      count = counter.get().getCount();
    }

    return ApiResponse.ok(count);
  }

  /**
   * 更新计数，自增或者清零
   *
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  ApiResponse create(
      @RequestHeader Map<String, String> headers, @RequestBody CounterRequest request) {
    String openid = headers.get("x-wx-openid");
    logger.info("/api/count post request, openid: {}, action: {}", openid, request.getAction());

    logger.info("headers：{} openid: {}", headers.size(), openid);
    headers.forEach((key, value) -> {
      logger.info(String.format("Header '%s' = %s", key, value));
    });

    openid = StringUtils.defaultIfBlank(openid, "_");
    Optional<Counter> curCounter = counterService.getCounter(openid);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setOpenid(openid);
      counter.setCount(count);
      counterService.upsertCount(counter);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        return ApiResponse.ok(0);
      }
      counterService.clearCount(openid);
      return ApiResponse.ok(0);
    } else {
      return ApiResponse.error("参数action错误");
    }
  }
}
