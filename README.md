# shadowkit

设备影子套件：用一套不可变的值对象，把「设备期望状态、设备上报状态、两者差异、版本乐观锁冲突、物模型校验」讲清楚。核心库零 Spring 依赖，Spring Boot 侧只做自动装配。

## 模块

| 模块 | 说明 |
| --- | --- |
| `shadowkit-core` | 纯 Java（零 Spring）核心库：设备影子、物模型、差异、版本冲突异常、内存存储。 |
| `shadowkit-starter` | Spring Boot 自动装配：注册存储与引擎 Bean，按 `X-Tenant-Id` 解析租户，提供 REST 端点。 |
| `shadowkit-sample` | 示例应用：演示下发期望、计算差异、设备上报同步与版本冲突保护。 |

## 核心概念

- **DeviceShadow**：某台设备的影子，不可变。保存期望状态 `desired` 与上报状态 `reported`，版本号 `version` 每次变更 +1。
  - `applyDesired(patch, now)` 合入期望，版本 +1
  - `applyReported(patch, now)` 合入上报，版本 +1
  - `delta()` 计算差异：desired 中值与 reported 不一致的属性
- **ThingModel**：物模型，声明设备支持哪些属性（`PropertyDefinition`）及其类型（`PropertyType`）。`validateState` 可校验一份状态是否越界或类型不符。
- **Delta**：差异对象，列出待设备同步的属性集合。`isEmpty()` 为 true 表示期望与上报已一致。
- **ShadowVersionConflictException**：乐观锁版本冲突——调用方传入的期望版本与影子当前版本不一致时抛出，用于防止并发覆盖。
- **ShadowkitEngine**：把存储、版本检查与差异计算串起来——`getShadow` / `applyDesired` / `applyReported` / `getDelta` / `delete`。

## 引入

```xml
<dependency>
  <groupId>io.github.nephets</groupId>
  <artifactId>shadowkit-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

配置（`application.yml`）：

```yaml
shadowkit:
  enabled: true
  tenant-header: X-Tenant-Id
```

## 代码示例

```java
ShadowkitEngine engine = ...;
ShadowkitTenant tenant = ShadowkitTenant.of("factory-1");

// 下发期望：新影子初始版本为 1，期望版本需传 1
DeviceShadow s = engine.applyDesired(tenant, "thermostat-1", Map.of("power", "on", "targetTemp", 26), 1L, Instant.now());
// 差异即为期望中尚未被上报覆盖的属性
engine.getDelta(tenant, "thermostat-1").ifPresent(d -> System.out.println(d.state()));

// 设备上报，差异随之清空
engine.applyReported(tenant, "thermostat-1", Map.of("power", "on", "targetTemp", 26), s.version(), Instant.now());

// 用过期版本再次下发会被拒绝
engine.applyDesired(tenant, "thermostat-1", Map.of("power", "off"), 1L, Instant.now()); // 抛 ShadowVersionConflictException
```

## HTTP 端点

所有端点都从 `X-Tenant-Id` 头解析租户；缺头返回 400。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/shadowkit/devices/{id}/shadow` | 查询设备影子（不存在返回 404） |
| PUT | `/shadowkit/devices/{id}/desired` | 合入期望（body: `state`,`version`；版本冲突返回 409） |
| PUT | `/shadowkit/devices/{id}/reported` | 合入上报（body: `state`,`version`；版本冲突返回 409） |
| GET | `/shadowkit/devices/{id}/delta` | 查询差异（不存在返回 404） |
| DELETE | `/shadowkit/devices/{id}/shadow` | 删除设备影子 |

```bash
curl -H "X-Tenant-Id: factory-1" -H "Content-Type: application/json" \
  -d '{"state":{"power":"on","targetTemp":26},"version":1}' \
  http://localhost:8080/shadowkit/devices/thermostat-1/desired
```

## 构建与测试

```bash
mvn -B verify
```

`shadowkit-core` 行覆盖率门禁为 0.75，整体为 0.60（由 CI 在 Linux 上校验）。

## 架构约束

`shadowkit-core` 严禁引入任何 `org.springframework.*` 依赖；CI 的 `architecture-boundaries` 任务会扫描其主源码，出现即失败。
