# 使用官方的 Nginx 镜像
FROM nginx:1.22-alpine

# 将 Vue.js 构建的静态文件复制到 Nginx 的默认静态文件目录
COPY dist /usr/share/nginx/html
COPY default.conf     /etc/nginx/conf.d/default.conf
# 暴露 80 端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]

