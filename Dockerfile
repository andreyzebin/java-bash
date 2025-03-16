FROM	 archlinux:latest

# Update the repositories
RUN	 pacman -Syy

# Install openssh
RUN	 pacman -S --noconfirm openssh

# Generate host keys
RUN  /usr/bin/ssh-keygen -A

# Add password to root user
RUN	 echo 'root:root' | chpasswd

# Fix sshd

RUN  sed -i -e 's/#UsePAM/UsePAM/' /etc/ssh/sshd_config
RUN  sed -i -e 's/UsePAM no/UsePAM yes/' /etc/ssh/sshd_config

RUN  sed -i -e 's/#PasswordAuthentication/PasswordAuthentication/' /etc/ssh/sshd_config
RUN  sed -i -e 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config

RUN  sed -i -e 's/#PermitRootLogin/PermitRootLogin/' /etc/ssh/sshd_config
RUN  sed -i -e 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# Expose tcp port
EXPOSE	 22

# Run openssh daemon
CMD	 ["/usr/sbin/sshd", "-D"]