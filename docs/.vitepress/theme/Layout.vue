<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-features-before>
      <section class="sig">
        <div class="sig-card">
          <span class="sig-label">TOPOLOGY · GATEWAY</span>
          <h3 class="sig-title">一个入口，两个服务，一条调用链</h3>
          <div class="sc-topo">
            <div class="sc-gw">Gateway :8080<em>统一入口 · IP 限流 · 过滤器</em></div>
            <div class="sc-fork"><span>↙</span><span>↘</span></div>
            <div class="sc-svcs">
              <div class="sc-svc"><b>user-service :8081</b><span>用户 CRUD</span></div>
              <div class="sc-svc"><b>order-service :8082</b><span>订单 CRUD</span></div>
            </div>
            <div class="sc-feign">OpenFeign：order ──→ user · 声明式调用，接口即契约</div>
          </div>
          <p class="sig-note">诚实边界：本项目落地了 Gateway 与 OpenFeign 两大件，负载均衡与熔断降级是文档里逐条列出的进阶方向——先把这条链路跑通，再谈更多组件。</p>
        </div>
      </section>
    </template>
    <template #home-features-after>
      <div class="sig-more"><a href="https://ibqy.github.io/" target="_blank" rel="noopener">← ibqy.github.io · 探索全部项目</a></div>
    </template>
  </Layout>
</template>
