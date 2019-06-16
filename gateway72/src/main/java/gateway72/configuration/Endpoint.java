package gateway72.configuration;

public class Endpoint {
    /** Endpoint host as logical name */
    private String serviceName;
    /** Endpoint path, usually with '/' at begin */
    private String url;
    /** Endpoint protocol + host + port. It's the resolved URL for the service from class Service. */
    private String serviceUrl;

    public String getServiceName() {
        return serviceName;
    }

    /**
     * serviceName OR serviceUrl must be set. If you set serviceName the service (with the URL) must be defined in the model.
     * @param serviceName
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
