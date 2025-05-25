package wanted.commerce.service.product;

public interface ProductCommandHandler {

    // 상품관리
    ProductDto.Product createProduct(ProductCommand.CreateProduct command);

    ProductDto.Product updateProduct(ProductCommand.UpdateProduct command);

    void deleteProduct(ProductCommand.DeleteProduct command);

    // 옵션관리
    ProductDto.Option addProductOption(ProductCommand.AddProductOption command);

    ProductDto.Option updateProductOption(ProductCommand.UpdateProductOption command);

    void deleteProductOption(ProductCommand.DeleteProductOption command);

    // 이미지
    ProductDto.Image addProductImage(ProductCommand.AddProductImage command);


}
