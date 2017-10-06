const routers = [
    {
        path: '/',
        redirect: '/metrics'
    },
    {
        path: '/metrics',
        component: (resolve) => require(['./views/metrics.vue'], resolve)
    }/*,
    {
        path: '/report',
        component: (resolve) => require(['./views/report.vue'], resolve)
    }*/
];
export default routers;