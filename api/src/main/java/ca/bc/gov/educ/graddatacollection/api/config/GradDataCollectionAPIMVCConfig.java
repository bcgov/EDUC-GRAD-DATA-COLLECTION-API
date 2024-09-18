package ca.bc.gov.educ.graddatacollection.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Pen reg api mvc config.
 *
 * @author Om
 */
@Configuration
public class GradDataCollectionAPIMVCConfig implements WebMvcConfigurer {

  /**
   * The Pen reg api interceptor.
   */
  @Getter(AccessLevel.PRIVATE)
  private final RequestResponseInterceptor requestResponseInterceptor;

  /**
   * Instantiates a new Pen reg api mvc config.
   *
   * @param requestResponseInterceptor the pen reg api interceptor
   */
  @Autowired
  public GradDataCollectionAPIMVCConfig(final RequestResponseInterceptor requestResponseInterceptor) {
    this.requestResponseInterceptor = requestResponseInterceptor;
  }

  /**
   * Add interceptors.
   *
   * @param registry the registry
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestResponseInterceptor).addPathPatterns("/**");
  }
}
