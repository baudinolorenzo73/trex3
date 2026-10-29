package it.lollo.trexbridge;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class FixTest {
    private static final long NOW=1800000000000L;
    private Map<String,String> sample(){
        Map<String,String> p=new HashMap<>();
        p.put("v","1");p.put("valid","true");p.put("session","watch_1");p.put("seq","1");
        p.put("lat","45.06");p.put("lon","7.39");p.put("observedAt",String.valueOf(NOW));return p;
    }
    @Test public void acceptsRealCoordinatesAndExpires(){
        Fix f=new Fix(sample(),NOW);assertEquals(45.06,f.lat,0.000001);assertTrue(f.fresh(NOW+7999));assertFalse(f.fresh(NOW+8000));
        assertFalse(f.fresh(NOW-1));
    }
    @Test public void rejectsInvalidCoordinatesSessionAndAge(){
        for(String[] bad:new String[][]{{"lat","NaN"},{"lon","181"},{"session","x&evil"},{"seq","0"},{"observedAt",String.valueOf(NOW-15001)}}){
            Map<String,String> p=sample();p.put(bad[0],bad[1]);
            assertThrows(RuntimeException.class,()->new Fix(p,NOW));
        }
    }
}
