data "google_compute_network" "default" {
    name = "default"
}

resource "google_compute_firewall" "null-port" {
    name = "null-port"
    network = data.google_compute_network.default.name
    allow {
        protocol = "icmp"
    }
    allow {
        protocol = "tcp"
        ports = [var.null_port]
    }
}

resource "google_compute_instance" "default" {
    name = "default"
    machine_type = var.null_machine_type
    zone = "${var.region}-${var.zone}"
    tags = [ "name", "default" ]
    boot_disk {
        auto_delete = true
        initialize_params {
            image = "projects/cos-cloud/global/images/${var.cos_image}"
            type="pd-standard"
        }
    }
    labels = {
        container-vm = var.cos_image
    }
    network_interface {
        network = "default"
        access_config {
        }
    }
    metadata = {
        gce-container-declaration = <<EOF
spec:
  containers:
  - name: null
    image: postgres
    stdin: false
    tty: false
    restartPolicy: Always
    env:
    - name: POSTGRES_USER
      value: ${var.null_user}
    - name: POSTGRES_PASSWORD
      value: ${var.null_pass}
    - name: POSTGRES_DB
      value: null
EOF
    }
}

locals {
    null_ip = google_compute_instance.default.network_interface[0].access_config[0].nat_ip
}

output "null_ip" {
    value = local.null_ip
}

resource "google_cloud_run_service" "tutorial-api" {
    name = "tutorial-api"
    location = var.region
    template {
        spec {
            containers {
                image = var.tutorial_api_image
                env {
                    name = "DATASOURCE_URL"
                    value = "r2dbc:pool:postgresql://${local.null_ip}:${var.null_port}/null"
                }
                env {
                    name = "DATASOURCE_USER"
                    value = var.null_user
                }
                env {
                    name = "DATASOURCE_PASS"
                    value = var.null_pass
                }
            }
        }
    }
    traffic {
        percent = 100
        latest_revision = true
    }
}

data "google_iam_policy" "public-access" {
    binding {
        role = "roles/run.invoker"
        members = [
            "allUsers"
        ]
    }
}

resource "google_cloud_run_service_iam_policy" "public-access" {
    location = google_cloud_run_service.tutorial-api.location
    project = google_cloud_run_service.tutorial-api.project
    service = google_cloud_run_service.tutorial-api.name
    policy_data = data.google_iam_policy.public-access.policy_data
}

output "url" {
    value = "${google_cloud_run_service.tutorial-api.status[0].url}"
}