provider "google" {
    credentials = file(var.credentials_path)
    project = "laplacian-tutorial"
    region = var.region
}