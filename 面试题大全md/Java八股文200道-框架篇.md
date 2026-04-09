# Java八股文200道

## 五、Mybatis框架

### 91、Mybatis底层执行流程？

```
MyBatis的底层执行流程：

1、读取配置文件：
MyBatis首先读取其全局配置文件（如mybatis-config.xml），该文件包含数据库连接信息、环境配置（如事务管理器类型和数据源类型）以及映射文件的加载路径等。
2、构建SqlSessionFactory：
使用配置文件中的信息，MyBatis构建SqlSessionFactory对象。SqlSessionFactory是MyBatis的核心对象之一，负责创建SqlSession对象。
3、创建SqlSession：
通过SqlSessionFactory对象，可以创建SqlSession对象。SqlSession是操作数据库的核心对象，它封装了JDBC连接，并提供了对数据库操作的基本方法。
4、动态代理生成Mapper接口实现：
MyBatis通过动态代理机制生成Mapper接口的实现类。这样，开发者就可以通过Mapper接口来调用数据库操作方法了。
5、获取Executor：
SqlSession内部会获取一个Executor对象。Executor是MyBatis操作数据库的执行器，它封装了JDBC的一些操作，并维护了一些缓存（如一级缓存和二级缓存）。MyBatis有三种Executor实现：SimpleExecutor、ReuseExecutor和BatchExecutor，它们分别对应不同的执行策略。
6、解析并执行SQL：
当调用Mapper接口的方法时，MyBatis会解析方法名或映射文件中的SQL语句，并根据传入的参数动态生成需要执行的SQL语句。然后，MyBatis通过Executor对象执行该SQL语句。在执行SQL语句之前，MyBatis还会进行输入参数的映射，即将Java类型的参数转换为数据库支持的类型。
7、处理Statement：
Executor在执行SQL语句时，会创建Statement对象（或PreparedStatement对象，取决于SQL语句的类型）。Statement对象用于向数据库发送SQL语句，并获取执行结果。MyBatis中的StatementHandler对象负责管理Statement对象，包括创建Statement、设置参数、执行SQL和8、获取结果等。
处理结果：
对于查询操作，MyBatis会将查询结果映射为Java对象或对象列表，并返回给调用者。这通常涉及到结果集的遍历和Java对象的创建与赋值。对于插入、更新和删除操作，MyBatis会返回影响的行数等信息。
9、关闭资源：
当操作完成后，需要显式关闭SqlSession对象以释放资源。
```



### 92、**#{}和${}的区别是什么？**

```
#{}是预处理占位符，MyBatis会对SQL语句进行预处理，将#{}替换为问号占位符，并调用PreparedStatement的set方法来赋值。这种方式可以有效地防止SQL注入，提高系统安全性。
$是字符串替换占位符，MyBatis在处理{}时，会直接将参数值替换到SQL语句中。这种方式虽然可以方便地进行字符串拼接，但会带来SQL注入的风险。
```



### 93、**Mybatis的一级、二级缓存是什么？**

```
Mybatis的一级、二级缓存是将查询的结果集保存到缓存中，下次做相同查询时直接从缓存中读取即可，无需访问数据库。
作用是：提高查询效率。

一级缓存：基于PerpetualCache的HashMap本地缓存，其存储作用域为SqlSession。当Session flush或close之后，该Session中的所有Cache就将清空。一级缓存默认是打开的。
二级缓存：与一级缓存其机制相同，默认也是采用PerpetualCache的HashMap存储。但不同在于其存储作用域为SqlSessionFactory，并且可自定义存储源，如Ehcache。二级缓存默认是不打开的，要开启二级缓存，需要实现Serializable序列化接口，并在映射文件中进行配置。
```



### 94、**Mybatis动态SQL有什么用？执行原理是什么？有哪些动态SQL？**

```
MyBatis动态SQL可以在Xml映射文件内，以标签的形式编写动态SQL，完成逻辑判断和动态拼接SQL的功能。

执行原理是：根据表达式的值完成逻辑判断，并动态拼接SQL语句。

MyBatis提供了9种动态SQL标签：trim、where、set、foreach、if、choose、when、otherwise、bind。
```





### 95、mapper映射文件配置有哪些要求？

```
使用MyBatis的mapper映射文件配置时，需要满足以下要求：

Mapper接口方法名和mapper.xml中定义的每个SQL的id相同。
Mapper接口方法的输入参数类型和mapper.xml中定义的每个SQL的parameterType的类型相同。
Mapper接口方法的输出参数类型和mapper.xml中定义的每个SQL的resultType的类型相同。
Mapper.xml文件中的namespace即是mapper接口的类路径。
```



### 96、**Mybatis 中 resultType 和 resultMap 的区别?**

```
MyBatis中，resultType和resultMap的区别如下：

resultType：简单映射，直接指定返回类型的别名或全类名，适用于列名和属性名一致的简单对象。
resultMap：复杂映射，允许定义详细的列到属性的映射关系，适用于需要精细控制映射的场景，如关联查询、嵌套结果等。
```



### 97、Mybatis分页插件实现原理？

```
MyBatis分页插件的实现原理简述如下：

插件机制：利用MyBatis的插件机制，通过实现Interceptor接口来拦截SQL执行。
配置插件：在MyBatis配置文件中注册分页插件，并指定其拦截目标和参数。
拦截SQL：当SQL语句执行时，分页插件拦截并解析SQL中的分页参数（如页码、每页条数）。
修改SQL：根据分页参数，修改原始SQL语句，添加LIMIT和OFFSET子句实现分页逻辑。
执行SQL：将修改后的SQL语句传递给数据库执行，并获取分页后的结果集。
返回结果：将分页结果集返回给调用方。
```

### 98、**MyBatis多表查询？**

```
MyBatis多表查询是指在MyBatis框架中，通过编写SQL语句来查询涉及两个或多个数据库表的数据。这种查询通常用于处理表之间的关系，如一对一、一对多和多对多关系。

MyBatis提供了resultMap、association和collection等标签，用于将多表查询的结果映射到Java对象中。其中，resultMap用于定义结果集的映射规则，association用于处理一对一关系，collection用于处理一对多关系。

```

### 99、Mybatis 的常用标签?

```
select 标签：封装查询 SQL 语句
insert 标签：封装新增 SQL 语句
update 标签：封装更新 SQL 语句
delete 标签：封装删除 SQL 语句

resultMap 标签：用来实现手动映射
association 标签：一对一关系映射描述。
collection 标签：一对多关系映射描述。

<sql>标签：sql片段
9种动态SQL标签：trim、where、set、foreach、if、choose、when、otherwise、bind。
```



### 100、模糊查询的实现方式

```
1、concat函数
2、"%"#{name}"%"
3、 '%${name}%'
4、bind拼接
5、拼好再传参
```



## 六、spring框架

### 101、Spring 是什么?核心功能是什么？

```
Spring 是一款目前主流的 Java EE 轻量级开源框架，其目的是用于简化 Java企业级应用的开发难度和开发周期。
spring的核心功能：IOC和AOP。

IOC：控制反转，将对象的创建操作交给spring容器完成。
AOP：面向切面编程，将和业务无关的公共模块抽离并封装起来，减少系统重复的代码，降低模块间的耦合度。
```

### 102、Spring 依赖注入的方式?

```
（1） setter 方法注入
（2）构造器注入
（3）p空间注入
（4）注解注入
```

### 103、Spring Bean 的作用域?

```
单例singleton
多例prototype
在web开发中还可以有：request，session，globle等
```

### 104、spring Bean实例化方式有哪些？

```
（1）构造方法方式（最常用，默认）
（2）静态工厂模式
（3）动态工厂模式
```

### 105、Spring 中@Resource 和@Autowired 的区别?

```
在Spring框架中，@Resource和@Autowired都是用于依赖注入的注解，但它们之间存在一些明显的区别。以下是对这两个注解的简单比较：

1、来源不同：
@Autowired是Spring框架定义的注解，专门用于Spring的依赖注入机制。
@Resource则是Java定义的注解，它来自于JSR-250规范（Java 250规范提案），是JSR-250提供的标准注解，但由Spring框架提供了具体实现。
2、装配方式默认值不同：
@Autowired默认按类型（byType）自动装配。如果Spring容器中有多个相同类型的bean，且没有通过@Qualifier注解指定具体的bean名称，那么@Autowired会尝试按名称（byName）装配，如果仍然无法确定唯一的bean，则会抛出异常。
@Resource默认按名称（byName）自动装配。如果找不到指定名称的bean，则会按类型（byType）进行装配。
3、支持的参数不同：
@Autowired只包含一个required参数，默认为true，表示注入的bean必须存在，否则就会注入失败。如果设置为false，则表示忽略当前要注入的bean，如果有直接注入，没有则跳过，不会报错。
@Resource包含多个参数，其中最重要的两个是name和type。name用于指定要注入的bean的名称，type用于指定要注入的bean的类型。
4、注解应用范围不完全相同：
@Autowired能够用在构造方法、成员变量、方法参数、注解上。
@Resource能用在类、成员变量和方法参数上（注意：在Spring框架中，通常不会将@Resource直接用于类上，而是用于类的成员变量或方法参数上以实现依赖注入）。另外，@Resource不支持构造方法注入，如果在构造方法上使用@Resource，则会导致错误。
```



### 106、spring框架由哪些模块构成？

```
7大模块：

1、Spring Core**：Core模块是Spring的核心类库，Spring的所有功能都依赖于该类库，Core主要实现
IOC功能，Spring的所有功能都是借助IOC实现的。
2、Spring AOP**：AOP模块是Spring的AOP库，为Spring容器管理的对象提供了对面向切面编程的支
持。
3、Spring DAO**：Spring 提供对JDBC的支持，对JDBC进行封装，允许JDBC使用Spring资源，同时还
基于AOP模块提供了事务管理。
4、Spring ORM**：Spring 的ORM模块提供对常用的ORM框架的管理和辅助支持，Spring支持常用的
Hibernate，ibtas，jdo等框架的支持，Spring本身并不对ORM进行实现，仅对常见的ORM框架进
行封装，并对其进行管理。
5、Spring Context**：Context模块提供框架式的Bean访问方式，其他程序可以通过Context访问
Spring的Bean资源。增加了对国际化、事件传播，以及验证等的支持，此外还提供了许多企业服
务及对模版框架集成的支持。
6、Spring Web**：WEB模块是建立于Context模块之上，提供了一个适合于Web应用的上下文。另外，
也提供了Spring和其它Web框架（如Struts1、Struts2、JSF）的集成。
7、Spring Web MVC**：WEB MVC模块为Spring提供了一套轻量级的MVC实现，是一个全功能的 Web
应用程序，容纳了大量视图技术，如 JSP、Velocity、POI等

```



### 107、BeanFactory和ApplicationContext有什么区别？

```
BeanFactory是Spring框架的基础容器，提供了基本的IOC功能；

而ApplicationContext是BeanFactory的子接口，提供了更多的功能，如国际化、事件传递等。

此外，BeanFactory采用延迟加载方式注入Bean，而ApplicationContext则是一次性加载所有Bean。
```



### 108、spring如何解决循环依赖？

```
（1）什么是循环依赖？
循环依赖就是循环引用，就是两个或多个Bean相互之间的持有对方，比如A引用B，B引用C，C引用A，则它们最终反映为一个环。

（2）解决方案：三级缓存。
比如：A、B两个类相互依赖.

初始化A的时候，第一步实例化A完成（生成对象工厂实例放入三级缓存），注入依赖属性B，一级缓存查询B没有，二级缓存查询B没有，

初始化B（生成对象工厂实例放入三级缓存），注入依赖属性A，一级缓存查询A没有，二级缓存查询A没有，三级缓存查询到A的对象工厂，需要AOP增强则生成A的代理对象，没有则直接创建A实例对象，并将A放入到二级缓存，注入A的代理对象完成，生成代理对象B，B移入一级缓存。

继续A属性注入（B的代理对象），然后可能还会依赖注入C对象流程和B一致，所有依赖注入完成后A初始化，生成A的代理对象，发现A的代理对象已存在，则跳过，放入一级缓存。此时A的代理对象也是提前生成的，但是仅针对循环依赖提前生成。
```

![12716a90a0c448fe90acd8a9e69ec803.png](https://i-blog.csdnimg.cn/blog_migrate/8cd968a13594b35d9b747bec8885ab20.jpeg)





### 109、spring  ioc底层原理

```
IOC的实现主要用到了3种技术：工厂模式、XML解析、反射。

IOC 执行过程：
（1）通过解析XML或者注解，拿到bean的信息。
（2）通过反射，创建bean对象。
（3）创建工厂对象，返回bean对象，存入spring容器。
```



### 110、spring用到哪些设计模式？

```
（1）工厂模式（beanfactory）
（2）单例模式（默认每个bean只有一个实例）
（3）代理模式（aop实现）
（4）观察者模式（事件驱动，监听器）
（5）模板模式（springjdbc的jdbcTemplate）
（6）适配器模式（springmvc）
```



### 111、spring bean的生命周期

```
Spring Bean生命周期的七个核心阶段：

1、实例化
通过反射机制创建Bean实例，此时仅分配内存空间，未进行依赖注入。
2、属性赋值（Populate Properties）
容器根据配置或注解完成依赖注入（如setter注入、构造器注入），填充Bean的属性值。
3、Aware接口回调
若Bean实现了特定Aware接口（如BeanNameAware、BeanFactoryAware、ApplicationContextAware），容器会调用对应方法传递上下文信息。
4、初始化前处理
执行所有注册的BeanPostProcessor的postProcessBeforeInitialization方法，例如AOP代理在此阶段生成。
5、初始化
调用用户定义的初始化逻辑，包括：
InitializingBean接口的afterPropertiesSet()方法。
XML配置的init-method或注解@PostConstruct标记的方法。
6、初始化后处理
执行BeanPostProcessor的postProcessAfterInitialization方法，完成最终的Bean修饰。
7、销毁
容器关闭时，若Bean实现了DisposableBean接口或配置了destroy-method/@PreDestroy，会调用对应方法释放资源。
```



### 112、什么是AOP？

```
一、AOP，即面向切面编程（Aspect-Oriented Programming），是一种软件设计范式。它通过将横切关注点（如日志记录、安全性、事务管理等）与业务逻辑分离，来提高代码的模块性和可维护性。

二、AOP概念：
joinpoint连接点：被增强的目标对象的方法，我们称为连接点。
pointcut切点：定义哪些连接点需要被增强，它是一个选择器。
advice通知：要做哪些增强，在目标方法前后增加的代码。
target目标对象：需要被增强的对象
proxy代理对象：增强后的对象。
weave织入：将通知插入到连接点（目标方法）的过程。
aspect切面：切点+通知。

三、AOP的实现方式
通过动态代理机制（如JDK动态代理或CGLIB代理）来创建代理对象。代理对象在调用目标对象的方法时，会根据通知的类型和切点表达式来执行相应的增强逻辑。

四、AOP的应用场景
（1）日志记录：程序的运行日志，用户的操作日志。
（2）事务管理：通过aop将多个操作放到事务中执行。
（3）缓存管理：将查询的结果放到缓存中。
（4）权限检查：判断用户受否有这个操作权限。
（5）性能监控：统计每个接口的调用时间。
（6）参数校验：在处理请求前，对参数进行数据校验。
（7）统一异常处理：如果发生了异常，进行异常处理。
```



### 113、spring aop底层原理

```
spring aop是面向切面编程。底层是通过动态代理实现的。
支持两种动态代理，jdk动态代理和cglib动态代理。
在Spring AOP中，如果目标对象实现了接口，Spring默认使用JDK动态代理。如果目标对象没有实现任何接口，Spring AOP会自动使用CGLIB动态代理。在Spring配置中，也可以强制使用CGLIB代理。
```



### 114、jdk动态代理和cglib动态代理区别

```
JDK动态代理：要求被代理的类必须实现一个或多个接口。代理对象会实现这些接口，并将方法调用委托给目标对象。
CGLIB动态代理：通过生成目标类的子类来实现代理。它不要求目标类必须实现接口，因此它适用于没有实现接口的类。
```



### 115、spring事务管理方式有哪些？

```
spring支持声明式事务管理与编程式事务管理。

声明式事务管理与编程式事务管理的核心区别如下：
1、实现方式
声明式：通过配置（XML或注解）自动管理事务。
编程式：通过编写代码手动管理事务。
2、代码耦合度
声明式：事务逻辑与业务逻辑分离，降低耦合。
编程式：事务逻辑与业务逻辑紧密绑定，耦合度高。
3、易用性与灵活性
声明式：易用，但某些复杂场景调试困难。
编程式：需要理解底层机制，但提供细粒度控制。
4、适用场景
声明式：适用于大型企业级应用，代码结构复杂。
编程式：适用于需要精确控制事务范围的场景。
```





## 七、springMVC框架

### 116、什么是**http 协议**

```
HTTP协议，全称为Hypertext Transfer Protocol，即超文本传输协议，是互联网上应用最为广泛的一种网络传输协议。
一、基本概念
HTTP协议定义了客户端（通常是浏览器）与服务器之间的通信规范，以实现对各种资源（如HTML页面、图像、音频、视频等）的传输和访问。每个请求都是独立的。


二、主要特点
1、基于请求响应模式：HTTP协议采用客户端-服务器架构模式，客户端向服务器发送请求，服务器返回相应的响应。
2、基于文本传输：HTTP协议使用ASCII码作为通信协议，每个请求和响应都是一条文本消息。
3、支持多媒体传输：HTTP协议可以传输多种类型的数据，如HTML、XML、JSON、图片、音频、视频等。
4、无状态：HTTP协议是一个无状态协议，不记录请求的客户端信息。

三、报文结构
HTTP报文分为请求报文和响应报文两种类型：
请求报文格式：请求行（包含请求方法、请求URL和HTTP协议版本）+ 头部信息（若干键值对）+ 空行 + 请求主体（可选）。
响应报文格式：状态行（包含HTTP状态码和状态信息）+ 头部信息（若干键值对）+ 空行 + 响应主体（可选）。

四、安全性
HTTP协议本身是不安全的，因为它在传输数据时所有内容都是明文，客户端和服务器端都无法验证对方的身份，也无法保证数据的安全性。为了解决这个问题，可以使用HTTPS协议，它是HTTP协议的安全版本，通过SSL/TLS加密技术来保证数据传输的安全性。
```

### 117、请求的方式有哪些？

```
HTTP/1.1协议中定义了9种请求方法来以不同方式操作指定的资源：

GET：请求指定的页面信息，并返回实体主体。通常用于请求数据，而不是提交数据。
POST：向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据包含在请求体中。
HEAD：与GET方法一样，只返回响应头信息，不返回实际数据。通常用于获取报头信息。
PUT：从客户端向服务器传送的数据取代指定的文档的内容。通常用于更新资源。
PATCH:从客户端向服务器传送的数据取代指定的文档的部分内容。通常用于更新资源部分内容。
DELETE：请求服务器删除指定的页面。通常用于删除资源。
CONNECT：预留给能够将连接改为管道方式的代理服务器。通常用于SSL加密服务器的链接（经由非加密的HTTP代理）。
OPTIONS：允许客户端查看服务器的性能。可以用来查询服务器支持的HTTP请求方法。
TRACE：回显服务器收到的请求，主要用于测试或诊断。
```

### 118、get和post请求的区别

```
GET请求没有请求体，POST请求有请求体。
GET请求的请求参数绑定在URL后面，POST请求参数放在请求体。
GET请求的请求参数会和URL一起显示在地址栏，而POST请求不会。
URL的长度有限制，所以绑定的请求参数长度有限制，而POST没有长度限制。
```

### 119、响应的状态码？常用的有哪些？

```
HTTP协议定义了多种不同的状态码，每个状态码都表示服务器对请求的响应状态。常见的状态码包括：

1xx（信息性状态码）：表示请求已经被接收，继续处理。
2xx（成功状态码）：表示请求已经被成功接收、理解和处理。
3xx（重定向状态码）：表示需要进行额外操作才能完成请求。
4xx（客户端错误状态码）：表示客户端发送的请求有误，服务器无法处理。
5xx（服务器错误状态码）：表示服务器在处理请求时发生了错误。

常见的状态码：
200　　　　响应成功
302　　　　重定向
400　　　　Bad Request客服端请求有语法错误，不能被服务器识别
401　　　　Unauthorized请求未经授权，这个状态码必须和WWW-Authenticate报头域一起使用
403　　　　Forbidden服务器收到请求，但是拒绝提供服务（认证失败）
404　　　　Not Found请求资源不存在
405　　　　请求方式不匹配
500　　　　Internal Server Error服务器内部错误
503　　　　Server Unavailable服务器当前不能处理客户端的请求，一段时间后可能恢复正常。
```

### 120、请说说MVC开发模型

```
MVC（Model-View-Controller）是一种软件设计模式，它将应用程序分为三个核心部分：模型（Model）、视图（View）和控制器（Controller）。

模型（Model）：代表应用程序的数据结构，负责处理业务逻辑和数据。
视图（View）：负责展示数据给用户，并接收用户的输入。它是用户界面的一部分。
控制器（Controller）：作为模型和视图之间的桥梁，接收用户的输入，调用模型和视图去完成相应的任务。
MVC模式的主要优势在于它将应用程序的不同方面（数据、用户界面和业务逻辑）分离开来，使得代码更加模块化、易于维护和扩展。这种分离还允许开发人员并行工作，提高开发效率。

在MVC模式中，用户与视图交互，视图将用户的请求传递给控制器，控制器再调用模型来处理请求，并将处理结果返回给视图进行展示。这样，应用程序的结构更加清晰，各部分之间的依赖关系更加明确。
```

### 121、Spring MVC如何处理HTTP请求和响应？

```
Spring MVC处理HTTP请求和响应的流程如下：
客户端发送HTTP请求到服务器，DispatcherServlet接收到请求后将其分发给相应的控制器Controller，控制器处理请求并生成相应的响应数据，然后返回逻辑视图名称给ViewResolver，ViewResolver将逻辑视图名称解析为实际的视图对象，最后视图生成HTML等响应内容并发送给客户端。
```



### 122、SpringMVC 拦截器与过滤器的区别?

```
SpringMVC中的拦截器（Interceptor）与过滤器（Filter）的区别：
一、定义与归属
拦截器：
是SpringMVC框架的组件。
实现了HandlerInterceptor接口（或继承实现了该接口的类，如HandlerInterceptorAdapter）。

过滤器：
是Servlet规范中的一部分，属于Servlet组件。
实现了javax.servlet.Filter接口。

二、执行时机与范围
拦截器：
在请求被DispatcherServlet接收后，但在Controller方法被调用之前执行（preHandle方法）。
在Controller方法调用后，但在视图渲染之前（postHandle方法）和请求处理完成后（afterCompletion方法）也可以执行。
只会拦截访问Controller方法的请求，不会拦截静态资源（如jsp、html等）。

过滤器：
在请求到达Servlet之前执行，并在响应返回给客户端之前执行。
可以拦截所有请求，包括静态资源和Controller方法的请求。

三、功能与用途
拦截器：
提供更精细的控制能力，可以在请求处理前后进行额外的处理，如日志记录、权限控制、缓存控制、资源处理、异常处理等。
可以获取IOC容器中的各个bean，因此可以方便地调用业务逻辑。

过滤器：
主要用于对请求和响应进行过滤和处理，如设置字符集、控制权限、控制转向、做一些业务逻辑判断等。
由于不依赖于Spring容器，因此无法直接获取IOC容器中的bean。

四、其他区别
1、创建与管理：
拦截器是由SpringMVC容器创建和管理的。
过滤器是由Servlet容器（如Tomcat）创建和管理的。
2、执行顺序：
过滤器在拦截器之前执行。
在一个应用中可以存在多个过滤器和拦截器，它们会按照声明的顺序依次执行。
3、触发机制：
拦截器是基于Java的反射机制实现的。
过滤器是基于函数回调机制实现的。

```

### 123、SpringMVC 执行流程（工作原理）?

```
在前后端分离的开发模式中，SpringMVC的执行流程相较于传统的MVC模式有所简化，因为它主要关注于处理客户端发送的请求并返回数据，而不再涉及视图的渲染。以下是SpringMVC在前后端分离开发中的执行流程：

1、客户端发送请求：
前端应用（如Vue.js、React等）通过AJAX或Axios等HTTP客户端向后端发送HTTP请求，这些请求通常是RESTful API请求。
2、DispatcherServlet拦截请求：
作为SpringMVC的前端控制器，DispatcherServlet拦截所有的HTTP请求。它是整个SpringMVC的控制中心，负责将请求转发给合适的处理器（Controller）。
3、处理器映射器（HandlerMapping）查找处理器：
DispatcherServlet调用处理器映射器，根据请求的URL查找对应的处理器（Controller）。处理器映射器保存了请求路径信息和处理器信息的映射关系。
4、处理器适配器（HandlerAdapter）调用处理器：
DispatcherServlet根据处理器映射器返回的处理器信息，找到能够执行该处理器的处理器适配器。处理器适配器负责调用具体的处理器方法。
5、处理器（Controller）处理请求：
处理器根据请求路径和请求参数，调用业务逻辑处理或数据库操作，生成相应的响应数据。在前后端分离的模式中，处理器的方法通常会添加@ResponseBody注解，表示该方法将返回数据而不是视图。
6、返回JSON数据：
处理器将处理结果作为JSON格式的数据返回给客户端。这是通过消息转换器（HttpMessageConverter）实现的，它负责将方法的返回结果转化为客户端期望的格式。
7、前端应用更新页面：
客户端接收到返回的数据后，根据数据更新页面或执行其他操作。这样，前后端之间就实现了数据的交互和页面的动态更新。
```



## 八、springboot框架

### 124、springboot的两个核心优点？

```
1、约定大于配置（Convention Over Configuration）
这个理念的核心思想是，通过遵循一系列预设的、广泛接受的约定，可以显著减少开发者在配置软件时所需的工作量。这些约定通常基于经验和实践，是开发者社区在长期使用过程中总结出来的最佳实践。

在 Spring Boot 中，“约定大于配置”的理念得到了很好的体现。例如，Spring Boot 提供了默认的日志配置、应用上下文配置、数据源配置等，这些配置通常已经足够满足大部分开发需求。开发者只需遵循这些约定，就可以快速启动和运行应用，而无需进行繁琐的配置工作。当然，如果开发者有特定的需求，也可以通过修改配置来覆盖这些默认值。

2、开箱即用（Out of the Box）
“开箱即用”则强调了软件的易用性和即时可用性。它意味着软件在首次安装或部署后，无需进行额外的配置或调整，就可以立即投入使用。

在 Spring Boot 中，“开箱即用”的理念体现在多个方面。例如，Spring Boot 提供了内嵌的 Servlet 容器（如 Tomcat、Jetty 等），使得开发者可以直接运行一个可执行的 JAR 文件来启动应用，而无需将应用部署到外部容器中。此外，Spring Boot 还提供了丰富的 Starter POM，这些 Starter POM 包含了常用的依赖和配置，使得开发者可以快速构建和运行应用。

综上所述，“约定大于配置”和“开箱即用”这两个理念共同构成了 Spring Boot 的核心优势。它们通过简化配置和提高易用性，使得开发者可以更加专注于业务逻辑的实现，而不是被繁琐的配置和部署问题所困扰。
```



### 125、SpringBoot 自动配置原理?

```
SpringBoot的自动配置原理是其框架的核心特性，旨在简化配置过程，提高开发效率。以下是该原理的简洁概述：

1、启用机制：通过@SpringBootApplication注解（其中包含了@EnableAutoConfiguration）来启用自动配置功能。
2、配置加载：Spring Boot会扫描类路径下的META-INF/spring.factories文件，该文件列出了所有自动配置类的全限定名。这些自动配置类包含了Spring Boot应用启动时所需的各种默认配置。
3、条件化配置：自动配置类利用@Conditional注解（如@ConditionalOnClass、@ConditionalOnBean、@ConditionalOnMissingBean等）来判断当前环境是否满足特定条件。只有当条件满足时，相应的Bean才会被注册到Spring容器中。
4、默认与覆盖：Spring Boot提供了一套默认配置，这些配置通常已经足够满足大部分开发需求。如果开发者需要自定义配置，可以通过在application.properties或application.yml配置文件中添加相应的配置项，或者通过编写自定义配置类来覆盖默认配置。

```

### 126、springboot配置文件有哪些？有什么区别？

```
SpringBoot配置文件主要有两种格式：properties和yml（或yaml）。这两种格式各有特点，适用于不同的场景。以下是它们的主要区别：

一、文件格式与结构
1、properties
格式：基于键值对的配置文件格式，每行由键和值组成，用等号（=）或冒号（:）分隔。
结构：文件结构简单，不支持复杂的嵌套结构，只适合配置简单的键值对属性。
示例：server.port=8080，spring.datasource.url=jdbc:mysql://localhost:3306/mydb。
2、yml（或yaml）
格式：基于缩进的配置文件格式，采用类似于JSON的键值对结构，使用冒号（:）来表示键值对，并通过缩进来表示层级关系。
结构：层次结构清晰，支持复杂的嵌套结构和列表，适合配置较为复杂的属性结构。
示例：
yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb

二、特性与优势
1、properties
优势：文件结构简单明了，易于理解和编辑，适合配置简单的键值对属性。
限制：不支持复杂的嵌套结构，不保证加载顺序（因为基于Hashtable实现，键值对存储不保证顺序）。
2、yml（或yaml）
优势：层次结构清晰，易于表达复杂的配置信息，支持字面量、对象和数组三种数据结构以及复合结构。
特性：大小写敏感，缩进表示层级关系（必须使用空格进行缩进，不能使用Tab键），支持中文内容（无需Unicode编码）。

三、优先级
application.properties优先级高于application.yml
```

### 127、Spring Boot 如何定义多套不同环境配置？

```
Spring Boot支持多环境配置，允许开发者为不同的运行环境（如开发、测试、生产等）定义不同的配置属性。
使用application-{profile}.properties或者yml文件

1、创建环境配置文件
在Spring Boot项目的src/main/resources目录下，为不同的环境创建特定的属性文件。这些文件遵循命名约定：application-{profile}.properties，其中{profile}是定义的环境名称，如dev（开发环境）、test（测试环境）和prod（生产环境）。

application-dev.properties：开发环境配置文件。
application-test.properties：测试环境配置文件。
application-prod.properties：生产环境配置文件。

2、配置文件内容
在每个环境配置文件中，定义该环境特有的配置属性。例如，在application-dev.properties中，可以设置数据库连接信息、服务器端口等。

3、激活特定配置文件
application.properties文件中的定义：spring.profiles.active=dev
```

### 128、SpringBoot解决跨域问题

```
SpringBoot解决跨域问题，可简洁总结为：

1、@CrossOrigin注解：直接加在Controller类或方法上，允许特定源的跨域请求。例如，@CrossOrigin(origins = "*")允许所有源。
2、配置类：通过实现WebMvcConfigurer接口，重写addCorsMappings方法，全局配置跨域。
3、过滤器：编写Cors过滤器，添加到Spring Security或Spring MVC的过滤器链中。

```

### 129、swagger是什么？常用注解？

```
Swagger是一个用于生成和描述RESTful API文档的框架，它能帮助开发者设计、构建、记录和使用API。

常用注解包括：
@Api：用于类上，标识这个类是Swagger的资源，用于生成API文档。
@ApiOperation：用于方法上，说明一个HTTP请求的操作，比如是GET还是POST，以及操作的简要说明。
@ApiImplicitParams和@ApiImplicitParam：用于方法上，表示一组请求参数的说明或单个请求参数的说明，包括参数的类型、是否必填等。
@ApiResponses和@ApiResponse：用于方法上，描述API可能的响应。@ApiResponses包含多个@ApiResponse，每个@ApiResponse描述一个可能的响应状态码和对应的响应消息体。
@ApiModel：用于类上，表示一个模型，通常用于描述请求或响应的数据结构。
@ApiModelProperty：用于类的字段上，描述模型属性的作用和数据类型。

这些注解帮助Swagger自动生成API文档，提高API的可读性和易用性。
```

### 130、**SpringBoot Starter是什么？**

```
SpringBoot Starter是一种用于简化依赖管理和配置的方式，它是Spring Boot中的一个重要概念。

一、定义
SpringBoot Starter是一个预定义的依赖关系集合，它包含了一组常用的依赖和配置，以便快速启动和构建特定类型的应用程序。使用Starter可以大大简化项目的依赖管理和配置工作。

二、作用
简化依赖管理：通过引入一个Starter依赖，就可以自动引入该Starter所依赖的其他库，无需手动添加多个依赖。
自动配置：SpringBoot会根据引入的Starter自动进行配置，无需手动编写大量的配置文件。
快速启动：由于依赖和配置都已预先定义好，因此可以快速启动应用程序。

三、命名规范
Spring官方Starter通常命名为spring-boot-starter-{name}，例如spring-boot-starter-web。对于非官方的Starter，通常遵循{name}-spring-boot-starter的格式，例如mybatis-spring-boot-starter。

四、自定义Starter
开发者也可以自定义自己的Starter，将常用的依赖和配置打包为一个Starter，方便在项目中复用和共享。
```

### 131、Spring常用注解？springMVC常用注解？Spring Boot的常用注解？

```
一、Spring 的常用注解
@Controller：一般用于表现层（控制层）。
@Service：一般用于业务层。
@Repository：一般用于持久层。
@Component：通用，通常用于三层架构之外。
@Scope：指定bean的作用域范围。

@Autowired：自动按照类型注入。
@Qualifier：与@Autowired一起使用，用于指定注入bean的名称

@Value：属性自动装配，支持简单类型。

@Configuration：用于类上，标识一个类作为配置类，其作用与xml配置文件相同。
@Bean：用于方法上，声明当前方法的返回值为一个bean。
@ComponentScan：一般与@Configuration注解一起使用，指定Spring扫描注解的包。

@Aspect：把当前类声明为切面类
@PointCut： 切点
@Before：前置通知，可以指定切入点表达式
@AfterReturning：后置【try】通知，可以指定切入点表达式
@AfterThrowing：异常【catch】通知，可以指定切入点表达式
@After：最终【finally】通知，可以指定切入点表达式
@Around：环绕通知，可以指定切入点表达式

@Transactional：用于接口定义、接口中的方法、类定义或类中的public方法上，表示该方法或类在事务环境中执行。

二、springMVC的常用注解
@RestController：用于类上，声明此类是一个Web控制器，处理Web请求并返回son数据。
@ResponseBody：用于方法上或返回值旁，将方法的返回值转换为指定格式（如JSON、XML）作为HTTP响应的内容返回给客户端。

@RequestMapping：用于类或方法上，映射HTTP请求到处理方法上。支持GET、POST等多种请求方法。
@GetMapping、@PostMapping、@PutMapping、@DeleteMapping等：是@RequestMapping的特化，分别用于映射HTTP GET、POST、PUT、DELETE等请求。

@RequestParam：用于方法参数上，将HTTP请求参数绑定到方法的参数上。
@PathVariable：用于方法参数上，从URI路径中提取参数值，将其映射到方法的参数上。
@RequestBody：用于方法参数上，将HTTP请求体的内容（如JSON、XML等）映射到一个Java对象。
@RequestHeader：用于方法参数上，获取Header中的值。

@ControllerAdvice：全局统一异常处理，最常见的是结合@ExceptionHandler注解用于全局异常的处理。
@ExceptionHandler：异常处理

三、springboot的常用注解
1、@SpringBootApplication
功能：这是Spring Boot应用程序的入口注解，它组合了@Configuration、@EnableAutoConfiguration和@ComponentScan三个注解的功能。
作用：用于标识一个主要的Spring Boot应用程序类，并启用Spring Boot的自动配置机制。
2、@EnableAutoConfiguration
功能：启用Spring Boot的自动配置机制。
3、@ConfigurationProperties
功能：基于类型安全的属性配置注入。
4、@ConditionalOnXXXX
（如@ConditionalOnBean、@ConditionalOnClass、@ConditionalOnMissingBean、@ConditionalOnMissingClass、@ConditionalOnProperty等）
功能：条件注解，根据是否满足某一个特定条件来决定是否加载指定的Bean。
5、测试相关注解（如Spring Boot测试框架中的注解）
@SpringBootTest等，用于测试Spring Boot应用程序。
```



## 九、MyBatis-Plus框架

### 132、MyBatis-Plus是什么？

```
MyBatis-Plus 是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。
MyBatis-Plus是MyBatis框架的一个扩展库，它提供了一系列方便的API和工具，可以简化常见的数据库操作，如CRUD操作、分页查询等。MyBatis-Plus还提供了代码生成器、自动填充、逻辑删除、性能分析等增强功能，进一步提高了开发效率。
```

### 133、**MyBatis-Plus中BaseMapper有哪些常用的方法？**

```
BaseMapper是MyBatis-Plus提供的一个接口，通过继承该接口，开发者可以获得一些默认的CRUD方法，如insert（插入）、updateById（根据主键更新）、deleteById（根据主键删除）、selectById（根据主键查询）等。此外，还有selectList（查询全部）、deleteBatchIds（批量删除）等常用方法。
```



### 134、**MyBatis-Plus中的逻辑删除是什么？如何配置和使用？**

```
MyBatis-Plus中的逻辑删除是一种数据删除状态的处理方式，它并非真正从数据库中物理删除数据，而是通过在数据表中添加一个特定的字段（如is_deleted或deleted）来标记数据是否被“删除”。
逻辑删除是指使用一个特殊的字段来表示数据库表中的一条记录是否被删除了（或是否存在）。例如，为数据库表中的每一条记录都增加一个is_delete字段，当is_delete为1时，表示该记录已经被删除了，当is_delete为0时，表示该记录还有效。这种方式的好处是可以保留数据的历史记录，方便后续进行数据恢复、数据分析等操作，同时避免了因误删等原因导致的数据丢失无法找回的问题。

application.yml的逻辑删除配置：
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 全局逻辑删除的实体字段名
      logic-delete-value: 1        # 逻辑已删除值（默认为1）
      logic-not-delete-value: 0    # 逻辑未删除值（默认为0）
```

### 135、MyBatis-Plus中常用的注解有哪些？

```
MyBatis-Plus中常用注解的简略说明：

@TableName：指定实体类对应的数据库表名。
@TableId：标识实体类的主键字段，并指定主键生成策略。
@TableField：映射实体类字段与数据库表字段，可控制字段的插入、更新和SQL解析行为。
@Version：用于乐观锁控制，标识版本字段。
@EnumValue：映射枚举类值到数据库表字段。
@TableLogic：实现逻辑删除功能，标识逻辑删除字段。

```



## 十、Spring Security安全框架

### 136、什么是Spring Security？核心功能？

```
Spring Security是一个基于Spring框架的安全框架，提供了完整的安全解决方案，包括认证、授权、攻击防护等功能。

核心功能：
认证：提供了多种认证方式，如表单认证、HTTP Basic认证、OAuth2认证等，可以与多种身份验证机制集成。
授权：提供了多种授权方式，如角色授权、基于表达式的授权等，可以对应用程序中的不同资源进行授权。

Spring Security通过配置安全规则和过滤器链来实现以上功能，可以轻松地为Spring应用程序提供安全性和保护机制。
```

### 137、Spring Security的原理?

```
Spring Security的核心原理是过滤器链（FilterChain）。
Spring Security会在Web应用程序的过滤器链中添加一组自定义的过滤器，这些过滤器可以实现身份验证和授权功能。
当用户请求资源时，Spring Security会拦截请求，并使用配置的身份验证机制来验证用户身份。如果身份验证成功，Spring Security会授权用户访问所请求的资源。
```



### 138、jwt是什么？

```
JWt定义：全称是json web token， 简单来说JWT就是通过可逆加密算法，生成一串包含用户、过期时间等关键信息的Token，每次请求服务器拿到这个Token解密出来就能得到用户信息，从而判断用户状态。
JWT由三部分组成：头部（Header）、载荷（Payload）和签名（Signature）。头部包含关于生成该JWT的信息以及所使用的算法类型；载荷包含要传递的数据，如身份信息和其他附属数据；签名使用密钥对头部和载荷进行签名，以验证其完整性。
```

### 139、Java权限管理系统之数据库设计？

权限五表：

```
一个用户可以拥有多个权限，同时一个权限也可以赋予多个用户，那么用户和权限就是多对多的关系，需要角色来维护和链接用户和权限之间的关系。通过用户关联角色，角色关联权限，从而达到用户和权限的一个间接关联关系。而用户和角色，角色和权限也是多对多的全系，从而也需要引入用户角色和角色权限两张中间表来实现。
```

![img](https://pic3.zhimg.com/v2-a81d2af49e16cb46ddbd9cc2d2e67478_1440w.jpg)

### 140、主键和外键



```  
主键和外键是关系型数据库中用于建立表之间关系的重要概念。

主键（Primary Key）
定义：主键是一列（或一组列），其值能够唯一地标识表中的每一行。每个表格只能有一个主键，并且主键值在整个表中必须是唯一的，不允许为空。
作用：
唯一标识表中的记录，确保数据的唯一性。
作为表中数据的索引，方便快速查询和定位特定行。
保证实体的完整性，防止重复数据的插入。
特点：
可以由数据库自动生成（如自增长整数），也可以由用户定义。
通常选择具有唯一性的字段作为主键，如用户ID、订单号等。


外键（Foreign Key）
定义：外键是一列（或一组列），它建立了表格之间的关联关系。外键在一个表格中指向另一个表格的主键，从而创建了表格之间的父子关系。
作用：
维护表之间的数据一致性和完整性。
允许跨表格进行数据查询和分析，以便更好地了解和分析不同的信息。
特点：
外键用于两个表的联系，通过将保存表中主键值的一列或多列添加到另一个表中，可创建两个表之间的链接。
两个表必须具有相同类型的属性，在该属性上有相同的值。该属性应为其中一个表的主键，在另外一个表设置为外键。
外键允许为空，这表示该字段不是必填的，但在引用完整性方面可能会有所影响。

```
