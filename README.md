# degressly-spring-replay
Java library for quickly setting up replay logging in Spring Applications.


## Quick Start

### Clone and Install package to your local repository
```bash
git clone https://github.com/degressly/degressly-spring-replay.git
mvn clean install
```

If you wish to use a legacy version of the library, checkout the relevant branch.

| Branch               | Java Version | Spring Version | Package Version            |
|----------------------|--------------|----------------|----------------------------|
| master               | 21           | 3.4.0          | 0.0.2-JAVA_21-Spring_3.4.0 |
| java_11_spring_2.7.7 | 11           | 2.7.7          | 0.0.2-JAVA_11-Spring_2.7.7 |

### Add the dependency to your project
```xml
 <dependency>
      <groupId>org.degressly</groupId>
      <artifactId>helper</artifactId>
      <version>0.0.2-JAVA_11-Spring_2.7.7</version>
    </dependency>
```

### Implement a DegresslyConfig class
```java
@Component
public class BaseDegresslyConfig implements org.degressly.helper.config.AbstractDegresslyConfig {

	@Override
	public String getTraceId() {
		return MDC.get(REQUEST_SEQ_NO);
	}

	@Override
	public boolean pickTrace(String traceId) {
		try {
			Integer samplingRateBPS = 10000;
			return !skipTrace(samplingRateBPS, traceId);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean skipTrace(Integer samplingBPS, String traceId) {
		return Objects.isNull(samplingBPS) || samplingBPS == 0 || StringUtils.isBlank(traceId)
				|| seqNo.hashCode() == Integer.MIN_VALUE || Math.abs(traceId.hashCode()) % 10000 > samplingBPS;
	}

}
```

### Add a component scan to your application
```java
...
@ComponentScan("org.degressly.helper")
...
public class YourApplication {
    ...
    public static void main(String[] args) {
        ...
        SpringApplication.run(YourApplication.class, args);
        ...
    }
    ...
}
```


### Add the degressly interceptor to your WebMvcConfigurerAdapter
```java
@Configuration
@EnableAutoConfiguration
public class InterceptorConfig extends WebMvcConfigurerAdapter {
    @Autowired
	private DegresslyInwardInterceptor degresslyInwardInterceptor;
	...
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	    ...
        registry.addInterceptor(degresslyInwardInterceptor);
        ...
    }
}
```

### Add logging config to your log4j2.xml
```xml
<Configuration>
    <Appenders>
        <RollingRandomAccessFile name="REQRESP" fileName="/tmp/yourpath_reqresp.log"
                                 filePattern="/tmp/yourpath_reqresp_%d{yyyy-MM-dd-HH}-%i.log.gz">
          <DefaultRolloverStrategy max="2"/>
          <PatternLayout>
            <pattern>
              %msg%n
            </pattern>
          </PatternLayout>
          <Policies>
            <TimeBasedTriggeringPolicy interval="1"/>
            <SizeBasedTriggeringPolicy size="1 GB" />
          </Policies>
        </RollingRandomAccessFile>
    </Appenders>
    <Loggers>
        <logger name="org.degressly.helper.logger.RequestResponseLogger" additivity="false">
            <AppenderRef ref="REQRESP" />
        </logger>
    </Loggers>
</Configuration>
```

And you're all set! The replay logs should get created in the specified path, which you can then ingest into the replay topic configured in degressly-core.
