using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using System.Text.Json;

namespace MyAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class FoodsController : ControllerBase
    {
        private readonly MyDBContext _context;
        private readonly HttpClient _httpClient;

        public FoodsController(
            MyDBContext context,
            IHttpClientFactory httpClientFactory)
        {
            _context = context;
            _httpClient = httpClientFactory.CreateClient();
        }

        [HttpGet("barcode/{barcode}")]
        public async Task<IActionResult> GetByBarcode(string barcode)
        {
            barcode = barcode.Trim();

            if (barcode.Length == 0 || !barcode.All(char.IsDigit))
                return BadRequest("Barcode không hợp lệ.");

            // 1. Tìm trong SQL Server
            var food = await _context.Foods
                .FirstOrDefaultAsync(f => f.Barcode == barcode);

            if (food != null)
            {
                return Ok(new FoodBarcodeResponse
                {
                    Found = true,
                    Source = "Database",
                    Barcode = food.Barcode,
                    Name = food.Name,
                    Brand = food.Brand,
                    ImageUrl = food.ImageUrl,
                    CategoryId = food.CategoryId
                });
            }

            // 2. Không có trong SQL → gọi Open Food Facts
            try
            {
                var url =
                    $"https://world.openfoodfacts.org/api/v3/product/{barcode}" +
                    "?fields=code,product_name,brands,image_front_url";

                using var request = new HttpRequestMessage(
                    HttpMethod.Get, url);

                request.Headers.UserAgent.ParseAdd(
                    "SmartFoodStudentProject/1.0 (contact: tranvantai4412@gmail.com)");

                using var response =
                    await _httpClient.SendAsync(request);

                if (response.StatusCode ==
                    System.Net.HttpStatusCode.NotFound)
                {
                    return Ok(new FoodBarcodeResponse
                    {
                        Found = false,
                        Source = "NotFound",
                        Barcode = barcode
                    });
                }

                if (!response.IsSuccessStatusCode)
                    return StatusCode(502,
                        "Không thể tra cứu Open Food Facts.");

                using var stream =
                    await response.Content.ReadAsStreamAsync();

                using var json =
                    await JsonDocument.ParseAsync(stream);

                var root = json.RootElement;

                if (!root.TryGetProperty("product", out var product) ||
                    product.ValueKind != JsonValueKind.Object)
                {
                    return Ok(new FoodBarcodeResponse
                    {
                        Found = false,
                        Source = "NotFound",
                        Barcode = barcode
                    });
                }

                string? GetString(string key)
                {
                    return product.TryGetProperty(key, out var value) &&
                           value.ValueKind == JsonValueKind.String
                        ? value.GetString()
                        : null;
                }

                var name = GetString("product_name");

                if (string.IsNullOrWhiteSpace(name))
                {
                    return Ok(new FoodBarcodeResponse
                    {
                        Found = false,
                        Source = "NotFound",
                        Barcode = barcode
                    });
                }

                return Ok(new FoodBarcodeResponse
                {
                    Found = true,
                    Source = "OpenFoodFacts",
                    Barcode = barcode,
                    Name = name,
                    Brand = GetString("brands"),
                    ImageUrl = GetString("image_front_url"),
                    CategoryId = null
                });
            }
            catch (HttpRequestException)
            {
                return StatusCode(502,
                    "Lỗi kết nối đến Open Food Facts.");
            }
            catch (TaskCanceledException)
            {
                return StatusCode(504,
                    "Open Food Facts phản hồi quá lâu.");
            }
            catch (JsonException)
            {
                return StatusCode(502,
                    "Dữ liệu Open Food Facts không hợp lệ.");
            }
        }
    }
}