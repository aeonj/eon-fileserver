<div class="layui-card">
    <div class="layui-card-header"><span><b>服务器组：</b>group2</span></div>
    <div class="layui-card-body">
        <div class="layui-row">
            <div class="layui-col-md8">
                <div class="layui-tab layui-tab-brief" lay-filter="tab-chart-group2">
                    <ul class="layui-tab-title">
                        <li lay-id="cpu" class="layui-this">CPU 使用率</li>
                        <li lay-id="mem">内存使用率</li>
                    </ul>
                    <div class="layui-tab-content eonadmin-pad0">
                        <div class="layui-tab-item layui-show">
                            <div id="chart-index-group2" style="width: 100%;height:300px;"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="layui-col-md4">
                <div class="layui-tab layui-tab-card" lay-filter="tab-info-group2">
                    <ul class="layui-tab-title">
                        <li class="layui-this">基本信息</li>
                        <li>高级信息</li>
                    </ul>
                    <div class="layui-tab-content eonadmin-pad0">
                        <div class="layui-tab-item layui-show">
                            <div style="width: 100%;height:300px;overflow-x: hidden; overflow-y: auto;">
                                <fieldset class="layui-elem-field layui-field-title">
                                    <legend>192.168.10.252</legend>
                                </fieldset>
                                <div class="eonadmin-cell">
                                    <div class="eonadmin-cell-title">
                                        <span class="layui-badge layui-bg-green">cpu：</span> 65% <span
                                            class="layui-badge layui-bg-green">mem：</span> 65% <span
                                            class="layui-badge layui-bg-green">状态：</span> <font
                                            color="red">Active</font>
                                    </div>
                                    <div class="eonadmin-cell-title">
                                        <span class="layui-badge layui-bg-green">ifTrunkServer：</span> true <span
                                            class="layui-badge layui-bg-green">最后更新日期：</span> 2920-02-02 12:00:00</font>
                                    </div>
                                </div>
                                <fieldset class="layui-elem-field layui-field-title">
                                    <legend>192.168.10.253</legend>
                                </fieldset>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    layui.use(['admin', 'echarts', 'element', 'util'], function (admin, echarts, element, util) {
        var $ = layui.jquery;
        var view = $('#group-main-index');

        element.on('tab(tab-info-group1)', function (data) {

        });
        element.on('tab(tab-chart-group1)', function (data) {
            console.log(JSON.stringify(data));
            if (data.index == 1) {
                createChart('chart-index-group1', 'group1', 'mem');
            } else {
                createChart('chart-index-group1', 'group1', 'cpu');
            }
        });
        element.on('tab(tab-chart-group2)', function (data) {
            console.log(JSON.stringify(data));
            if (data.index == 1) {
                createChart('chart-index-group2', 'group2', 'mem');
            } else {
                createChart('chart-index-group2', 'group2', 'cpu');
            }
        });

        function createChart(id, group, type) {
            var myChart = echarts.init(document.getElementById(id), layui.echartsTheme);
            myChart.showLoading({
                text: "图表数据正在努力加载...",
                effect: 'whirling',//'spin' | 'bar' | 'ring' | 'whirling' | 'dynamicLine' | 'bubble'
                textStyle: {
                    fontSize: 20
                }
            });
            admin.get({
                url: '/manage/getPerformanceLine',
                api: 'getPerformance',
                data: {groupName: group, type: type},
                success: function (res) {
                    if (res) {
                        var series = [],
                                legend_datas = [];
                        res.forEach(function (ele) {
                            series.push({
                                type: 'line',
                                name: ele.name,
                                markPoint: {
                                    data: [{
                                        type: 'max',
                                        name: '最大值'
                                    }, {
                                        type: 'min',
                                        name: '最小值'
                                    }]
                                },
                                lineStyle: {
                                    normal: {
                                        width: 3,
                                        shadowBlur: 40,
                                        shadowOffsetY: 10
                                    }
                                },
                                smooth: false,
                                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                                data: ele.data
                            });
                            legend_datas.push(ele.name);
                        });
                        var option = {
                            tooltip: {
                                trigger: 'axis'
                            },
                            toolbox: {
                                show: true,
                                feature: {
                                    //mark : {show : true},
                                    //dataView : {show : true,readOnly : false},
                                    magicType: {
                                        show: true, type: ['line', 'bar']
                                    },
                                    //restore : {show : true},
                                    saveAsImage: {
                                        show: true
                                    }
                                }
                            },

                            xAxis: {
                                type: 'time'
                            },
                            yAxis: {
                                type: 'value',
                                axisLabel: {
                                    formatter: '{value} %'
                                },
                                splitArea: {
                                    show: true
                                },
                                axisPointer: {snap: true}
                            },
                            legend: {
                                data: legend_datas
                            },
                            series: series
                        };

                        myChart.setOption(option);
                    }
                },
                complete: function () {
                    myChart.hideLoading();
                }
            });
        }

        createChart('chart-index-group1', 'group1', 'cpu');
        createChart('chart-index-group2', 'group2', 'cpu');

    });
</script>
