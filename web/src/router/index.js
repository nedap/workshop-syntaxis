import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/assignment',
      name: 'assignment',
      component: () => import('../views/AssignmentView.vue'),
    },
    {
      path: '/clients',
      name: 'clients',
      component: () => import('../views/ClientsView.vue'),
    },
    {
      path: '/client/:clientId',
      name: 'client',
      component: () => import('../views/ClientView.vue'),
    },
  ],
})

export default router
