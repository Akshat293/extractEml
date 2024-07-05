connector.name=hive-hadoop2
hive.s3.aws-access-key=<YOUR_AWS_ACCESS_KEY>
hive.s3.aws-secret-key=<YOUR_AWS_SECRET_KEY>
hive.s3.endpoint=<S3_ENDPOINT>          # Optional: e.g., s3.amazonaws.com for AWS or custom endpoint for other S3-compatible storage
hive.s3.bucket=<YOUR_BUCKET_NAME>       # Optional: if you want to specify a bucket
hive.s3.path-style-access=true          # Optional: set to true if you are using a custom endpoint
hive.metastore=s3                      # Optional: for using S3 as a metastore
hive.s3.staging-directory=/mnt/trino-s3-staging  # Local directory for S3 staging

# Optional: Specify additional configurations
hive.s3.ssl.enabled=true               # Enable SSL connection to S3 (default is true)
hive.s3.use-instance-credentials=false # Use instance credentials (e.g., for EC2 instances with IAM roles)
