using System.ComponentModel.DataAnnotations;

namespace MyAPI.DTOs
{
    public class RegisterRequest
    {
        public string Username { get; set; }

        [EmailAddress(ErrorMessage = "Email không đúng định dạng.")]
        public string Email { get; set; }

        [MinLength(8, ErrorMessage = "Password phải có ít nhất 8 ký tự.")]
        public string Password { get; set; }
    }
}