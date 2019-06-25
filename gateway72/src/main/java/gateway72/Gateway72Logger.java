package gateway72;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gateway72Logger {
    public static Gateway72Logger instance = new Gateway72Logger();
    
    public void boot(String msg) {
        log(msg);
    }
    
    public void dump(String msg) {
        log(msg);
    }

    public void rewrite(boolean success, String in, String out) {
        log(success, "IN : " + in);
        log(success, "OUT: " + out);
    }

    public void role(boolean success, String msg) {
        log(success, msg);
    }

    public void log(String msg) {
        System.out.println(msg);
    }
    
    protected void log(boolean success, String msg) {
        String m = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + " [Gateway]  " + msg;
        if (success) {
            System.out.println(m);
        } else {
            System.err.println(m);
        }
    }

    public void warn(String msg) {
        System.err.println("[WARN] " + msg);
    }
}
