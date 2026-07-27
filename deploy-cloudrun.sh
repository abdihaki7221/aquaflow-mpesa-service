#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════
# deploy-cloudrun.sh — deploy AquaFlow M-Pesa Service to Cloud Run
# Run from the backend repo root (where the Dockerfile lives).
#
#   chmod +x deploy-cloudrun.sh
#   ./deploy-cloudrun.sh
# ═══════════════════════════════════════════════════════════════
set -euo pipefail

# ---- EDIT THESE THREE ----
PROJECT_ID="your-gcp-project-id"
REGION="europe-west1"          # pick one close to you (see guide)
SERVICE="aquaflow-mpesa"       # your Cloud Run service name
# --------------------------

gcloud config set project "$PROJECT_ID"

echo "==> Deploying $SERVICE to Cloud Run ($REGION)..."
gcloud run deploy "$SERVICE" \
  --source . \
  --region "$REGION" \
  --platform managed \
  --allow-unauthenticated \
  --port 8080 \
  --cpu 1 \
  --memory 1Gi \
  --min-instances 0 \
  --max-instances 4 \
  --timeout 300 \
  --cpu-boost \
  --env-vars-file cloudrun.env.yaml

echo ""
echo "==> Done. Service URL:"
gcloud run services describe "$SERVICE" --region "$REGION" \
  --format='value(status.url)'
