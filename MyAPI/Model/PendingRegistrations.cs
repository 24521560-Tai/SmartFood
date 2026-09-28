namespace MyAPI.Model
{
    public class PendingRegistration
    {
        public int Id { get; set; }

        public string Email { get; set; } = null!;

        public string PasswordHash { get; set; } = null!;

        public string OTP { get; set; } = null!;

        public DateTime ExpiryTime { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.Now;
    }
}