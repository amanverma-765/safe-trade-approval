# Setting Up Domain and HTTPS SSL on Azure Ubuntu VM

## Prerequisites

1. An Azure Ubuntu VM.
2. A domain name (e.g., stalogin.com).
3. SSH access to the VM.

## Step 1: Update and Upgrade the System

```bash
sudo apt update && sudo apt upgrade -y
```

## Step 2: Install Nginx

```bash
sudo apt install nginx -y
```

## Step 3: Configure Nginx

1. Create a new Nginx configuration file for your domain:

```bash
sudo nano /etc/nginx/sites-available/stalogin.com
```

2. Add the following configuration to the file:

```nginx
server {
    listen 80;
    server_name stalogin.com www.stalogin.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

3. Enable the configuration by creating a symbolic link:

```bash
sudo ln -s /etc/nginx/sites-available/stalogin.com /etc/nginx/sites-enabled/
```

4. Test the Nginx configuration and restart Nginx:

```bash
sudo nginx -t
sudo systemctl restart nginx
```

## Step 4: Install Certbot and Obtain SSL Certificate

1. Install Certbot:

```bash
sudo apt install certbot python3-certbot-nginx -y
```

2. Obtain the SSL certificate:

```bash
sudo certbot --nginx -d stalogin.com -d www.stalogin.com
```

3. Follow the prompts to complete the SSL certificate installation.

## Step 5: Update the .env File

1. Open the `.env` file in your project:

```bash
nano /path/to/your/project/.env
```

2. Update the `NEXTAUTH_URL` to use HTTPS:

```env
NEXTAUTH_URL=https://stalogin.com
```

3. Save and close the file.

## Step 6: Restart Your Application

1. Restart your application to apply the changes:

```bash
pm2 restart your-application-name
```

Replace `your-application-name` with the actual name of your application.

## Conclusion

Your application should now be accessible at `https://stalogin.com` with HTTPS SSL configured.
