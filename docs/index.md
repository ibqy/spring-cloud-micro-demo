---
layout: home

hero:
  name: Spring Cloud 微服务教学
  text: 最小可跑的用户-订单微服务
  tagline: Gateway 网关 → order 服务 → user 服务的完整调用链路，演示服务拆分、OpenFeign 声明式调用与网关路由
  actions:
    - theme: brand
      text: 开始学习 →
      link: /01-architecture
    - theme: alt
      text: GitHub 源码
      link: https://github.com/ibqy/spring-cloud-micro-demo

features:
  - icon: 🚪
    title: Gateway 网关（8080）
    details: 统一入口、路由转发，体会网关在微服务架构中的位置
  - icon: 📦
    title: order 服务（8082）
    details: 订单业务，通过 OpenFeign 跨服务调用 user 服务
  - icon: 👤
    title: user 服务（8081）
    details: 用户数据提供方，被下游声名式调用
  - icon: 🔗
    title: OpenFeign 服务调用
    details: 声明式 HTTP 客户端，像调本地方法一样调远程服务
  - icon: 📝
    title: 教学知识点清单
    details: 诚实地标注本仓库覆盖与未覆盖的内容，以及进阶方向
---
