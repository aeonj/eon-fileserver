package eon.hg.fileserver.controller;


import eon.hg.fileserver.authorization.annotation.Authorization;
import eon.hg.fileserver.authorization.annotation.CurrentUser;
import eon.hg.fileserver.authorization.manager.TokenManager;
import eon.hg.fileserver.authorization.model.TokenModel;
import eon.hg.fileserver.model.User;
import eon.hg.fileserver.service.UserService;
import eon.hg.fileserver.util.body.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenManager tokenManager;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public ResponseEntity<ResultBody> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.getUserByAccount(username);

        if (user == null ||  //未注册
                !user.getPsw().equals(password)) {  //密码错误
            //提示用户名或密码错误
            return new ResponseEntity(ResultBody.failed("账号或密码错误"), HttpStatus.OK);
        }
        //生成一个token，保存用户登录状态
        TokenModel model = tokenManager.createToken(user.getId());
        model.setRealname(user.getName());
        return new ResponseEntity(ResultBody.success().addObject(model), HttpStatus.OK);
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    @Authorization
    public ResponseEntity<ResultBody> logout(@CurrentUser User user) {
        tokenManager.deleteToken(user.getId());
        return new ResponseEntity(ResultBody.success(), HttpStatus.OK);
    }

    @RequestMapping(value ="/confirm" ,method = RequestMethod.GET)
    @Authorization
    public ResponseEntity<ResultBody> confirm(@CurrentUser User user) {
        return new ResponseEntity(ResultBody.success(), HttpStatus.OK);
    }

}
