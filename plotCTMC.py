from numpy import loadtxt
import matplotlib.pyplot as plt
import os

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.1_0.1','ShopScenarionPM_10000_6_ClerkUtilisation_.data')
x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)
ax.plot(x,ut, label=r'$\alpha = \frac{1}{10}$')
# ax.fill_between(x, y-se, y+se)

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.2.csv'), delimiter=';', unpack=True)
ax.plot(x,ut, label=r'$\alpha = \frac{1}{5}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.5.csv'), delimiter=';', unpack=True)
ax.plot(x,ut, label=r'$\alpha = \frac{1}{2}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-1.0.csv'), delimiter=';', unpack=True)
ax.plot(x,ut, label=r'$\alpha = 1$')

ax.set_title('Clerk Utilisation ' + r'$N = 5, K = 1, \beta = \frac{1}{10}$')

plt.legend()
fig.savefig('Clerk_Utilisation_CTMC.png')
plt.close(fig)

###########################################

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.1_0.1','ShopScenarionPM_10000_6_CustomerServed_.data')
x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)
ax.plot(x,serv, label=r'$\alpha = \frac{1}{10}$')
# ax.fill_between(x, y-se, y+se)

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.2.csv'), delimiter=';', unpack=True)
ax.plot(x,serv, label=r'$\alpha = \frac{1}{5}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.5.csv'), delimiter=';', unpack=True)
ax.plot(x,serv, label=r'$\alpha = \frac{1}{2}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-1.0.csv'), delimiter=';', unpack=True)
ax.plot(x,serv, label=r'$\alpha = 1$')

ax.set_title('Customer Served ' + r'$N = 5, K = 1, \beta = \frac{1}{10}$')

plt.legend()
fig.savefig('Customer_Served_CTMC.png')
plt.close(fig)

####################################

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.1_0.1','ShopScenarionPM_10000_6_CustomerWaiting_.data')
x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)

ax.plot(x,wait, label=r'$\alpha = \frac{1}{10}$')
# ax.fill_between(x, y-se, y+se)

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.2.csv'), delimiter=';', unpack=True)
ax.plot(x,wait, label=r'$\alpha = \frac{1}{5}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.5.csv'), delimiter=';', unpack=True)
ax.plot(x,wait, label=r'$\alpha = \frac{1}{2}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-1.0.csv'), delimiter=';', unpack=True)
ax.plot(x,wait, label=r'$\alpha = 1$')

ax.set_title('Customer Waiting ' + r'$N = 5, K = 1, \beta = \frac{1}{10}$')

plt.legend()
fig.savefig('Customer_Waiting_CTMC.png')
plt.close(fig)

###################################à

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.1_0.1','ShopScenarionPM_10000_6_CustomerWandering_.data')
x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)

ax.plot(x,wander, label=r'$\alpha = \frac{1}{10}$')
# ax.fill_between(x, y-se, y+se)

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)
ax.plot(x,wander, label=r'$\alpha = \frac{1}{5}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)
ax.plot(x,wander, label=r'$\alpha = \frac{1}{2}$')

x, ut, wait, serv, wander = loadtxt(os.path.join(os.getcwd(),'data','collect-5-1-0.1-0.1.csv'), delimiter=';', unpack=True)
ax.plot(x,wander, label=r'$\alpha = 1$')

ax.set_title('Customer Cannot Enter ' + r'$N = 5, K = 1, \beta = \frac{1}{10}$')

plt.legend()
fig.savefig('Customer_Cannot_Enter_CTMC.png')
plt.close(fig)
