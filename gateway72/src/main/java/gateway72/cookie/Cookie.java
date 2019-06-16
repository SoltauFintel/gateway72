package gateway72.cookie;

import javax.servlet.http.HttpServletResponse;

import gateway72.Gateway72Logger;

public class Cookie {
    private final String name;

    public Cookie(String name) {
        this.name = name;
    }

	public String get(javax.servlet.http.HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    public void set(String value, HttpServletResponse response) {
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(name, value);
        cookie.setDomain("");
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30 /* 30 days */);
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        Gateway72Logger.instance.log("[Cookie] created cookie '" + name + "' for '" + value + "'");
    }

    //	TODO
//    public void extendLifeTime(String value, Response res) {
//        set(value, res, "extends-cookie-life-time");
//    }

	public void remove(javax.servlet.http.HttpServletRequest req, HttpServletResponse response) {
        for (javax.servlet.http.Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals(name)) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                Gateway72Logger.instance.log("[Cookie] removed cookie '" + name + "' for '" + cookie.getValue() + "'."
                        + " Session ID: " + req.getSession().getId());
            }
        }	    
    }
}
