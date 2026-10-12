namespace MyAPI.DTOs
{
    public class FoodBarcodeResponse
    {
        public bool Found { get; set; }
        public string Source { get; set; } = "";
        public string Barcode { get; set; } = "";
        public string? Name { get; set; }
        public string? Brand { get; set; }
        public string? ImageUrl { get; set; }
        public int? CategoryId { get; set; }
    }
}