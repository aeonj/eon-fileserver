package eon.hg.fileserver.util.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CachePool extends HashMap<String,Map<String,Object>> {

}
