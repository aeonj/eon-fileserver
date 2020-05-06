//请求URL
layui.define([],function(exports){
    exports('api',{
        login:'json/login.js',
        getMenu:'json/menu.js',
        initPerformance:'json/performance_init.json',
        getPerformanceLine:'json/performance_getLine.json',
        initCapacity:'json/capacity_init.json',
        initNetTraffic:'json/netTraffic_init.json',
        getNetTrafficLine:'json/netTraffic_getLine.json',
        getWarningList:'json/warning_list.json',
        insertWarningData: 'json/success.json',
        getNoticeList:'json/notice_list.json',
        insertNoticeData: 'json/success.json',
        getGoods:'json/goods.js'
    });
});