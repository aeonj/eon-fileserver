<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>分布式监控管理后台登录</title>
    <link rel="stylesheet" href="/webjars/layui/2.5.6/css/layui.css" media="all"/>
    <link rel="stylesheet" href="/css/login.css" media="all"/>
    <style>
        /* 覆盖原框架样式 */
        .layui-elem-quote{background-color: inherit!important;}
        .layui-input, .layui-select, .layui-textarea{background-color: inherit; padding-left: 30px;}
    </style>
</head>
<body>
<!-- Head -->
<div class="layui-fluid">
    <div class="layui-row layui-col-space15">
        <div class="layui-col-sm12 layui-col-md12 eon_mar_01">
            <blockquote class="layui-elem-quote">分布式监控管理后台登录界面</blockquote>
        </div>
    </div>
</div>
<!-- Head End -->

<!-- Carousel -->
<div class="layui-row">
    <div class="layui-col-sm12 layui-col-md12">
        <div class="layui-carousel eon_login_height" id="eonlogin" lay-filter="eonlogin">
            <div carousel-item="">
                <div>
                    <div class="eon_login_cont"></div>
                </div>
                <#--<div>-->
                    <#--<div class="background">-->
                        <#--<span></span><span></span><span></span>-->
                        <#--<span></span><span></span><span></span>-->
                        <#--<span></span><span></span><span></span>-->
                        <#--<span></span><span></span><span></span>-->
                    <#--</div>-->
                <#--</div>-->
            </div>
        </div>
    </div>
</div>
<!-- Carousel End -->

<!-- Footer -->
<div class="layui-row">
    <div class="layui-col-sm12 layui-col-md12 eon_center eon_mar_01">
        © 2019 - 2020 分布式监控管理后台登录界面版权所有
    </div>
</div>
<!-- Footer End -->



<!-- LoginForm -->
<div class="eon_lofo_main">
    <fieldset class="layui-elem-field layui-field-title eon_mar_02">
        <legend>管理员登录</legend>
    </fieldset>
    <div class="layui-row layui-col-space15">
        <form class="layui-form eon_pad_01" action="">
            <div class="layui-col-sm12 layui-col-md12">
                <div class="layui-form-item">
                    <input type="text" name="username" lay-verify="required|userName" autocomplete="off" placeholder="账号" class="layui-input">
                    <i class="layui-icon layui-icon-username eon_lofo_icon"></i>
                </div>
            </div>
            <div class="layui-col-sm12 layui-col-md12">
                <div class="layui-form-item">
                    <input type="password" name="password" lay-verify="required|pass" autocomplete="off" placeholder="密码" class="layui-input">
                    <i class="layui-icon layui-icon-password eon_lofo_icon"></i>
                </div>
            </div>
            <div class="layui-col-sm12 layui-col-md12">
                <div class="layui-row">
                    <div class="layui-col-xs7 layui-col-sm7 layui-col-md7">
                        <div class="layui-form-item">
                            <input type="text" name="verifycode" id="vercode" lay-verify="required|vercodes" autocomplete="off" placeholder="验证码" class="layui-input" maxlength="4">
                            <i class="layui-icon layui-icon-vercode eon_lofo_icon"></i>
                        </div>
                    </div>
                    <div class="layui-col-xs4 layui-col-sm4 layui-col-md4">
                        <div class="eon_lofo_vercode eonVerCode" onclick="eonVerCode()"></div>
                    </div>
                </div>
            </div>
            <div class="layui-col-sm12 layui-col-md12">
                <button class="layui-btn layui-btn-fluid" lay-submit="" lay-filter="form-login">立即登录</button>
            </div>
        </form>
    </div>
</div>
<!-- LoginForm End -->


<!-- Jquery Js -->
<script type="text/javascript" src="/js/jquery-3.2.1.min.js"></script>
<!-- Layui Js -->
<script type="text/javascript" src="/webjars/layui/2.5.6/layui.js"></script>
<!-- Jqarticle Js -->
<script type="text/javascript" src="/js/jparticle.min.js"></script>
<!-- zwVerificationCode Js-->
<script type="text/javascript" src="/js/zwVerificationCode.js"></script>
<script>
    layui.use(['carousel', 'form'], function(){
        var carousel = layui.carousel
                ,form = layui.form;

        //自定义验证规则
        form.verify({
            userName: function(value){
                if(value.length < 5){
                    return '账号至少得5个字符';
                }
            }
            ,pass: [/^[\S]{6,12}$/,'密码必须6到12位，且不能出现空格']
            ,vercodes: function(value){
                //获取验证码
                var eonVerCode = $(".eonVerCode").html();
                if(value!=eonVerCode){
                    return '验证码错误（区分大小写）';
                }
            }
            ,content: function(value){
                layedit.sync(editIndex);
            }
        });

        //监听提交
        form.on('submit(form-login)', function(data){
            layer.alert(JSON.stringify(data.field),{
                title: '最终的提交信息'
            })
            return false;
        });


        //设置轮播主体高度
        var eon_login_height = $(window).height()/1.3;
        var eon_car_height = $(".eon_login_height").css("cssText","height:" + eon_login_height + "px!important");


        //Login轮播主体
        carousel.render({
            elem: '#eonlogin'//指向容器选择器
            ,width: '100%' //设置容器宽度
            ,height:'eon_car_height'
            ,arrow: 'always' //始终显示箭头
            ,anim: 'fade' //切换动画方式
            ,autoplay: true //是否自动切换false true
            ,arrow: 'hover' //切换箭头默认显示状态||不显示：none||悬停显示：hover||始终显示：always
            ,indicator: 'none' //指示器位置||外部：outside||内部：inside||不显示：none
            ,interval: '5000' //自动切换时间:单位：ms（毫秒）
        });

        //监听轮播--案例暂未使用
        carousel.on('change(eonlogin)', function(obj){
            var loginCarousel = obj.index;
        });

        //粒子线条
        $(".eon_login_cont").jParticle({
            background: "rgba(0,0,0,0)",//背景颜色
            color: "#fff",//粒子和连线的颜色
            particlesNumber:100,//粒子数量
            //disableLinks:true,//禁止粒子间连线
            //disableMouse:true,//禁止粒子间连线(鼠标)
            particle: {
                minSize: 1,//最小粒子
                maxSize: 3,//最大粒子
                speed: 30,//粒子的动画速度
            }
        });

    });

</script>
</body>
</html>
