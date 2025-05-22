package wanted.commerce.repository.projection;

public interface ProductImageProjection {
    Long getId();
    String getUrl();
    String getAltText();
    Boolean getIsPrimary();
    Integer getDisplayOrder();
    Long getProductId();
    Long getOptionId();
}