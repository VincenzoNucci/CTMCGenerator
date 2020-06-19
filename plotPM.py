from numpy import loadtxt
import matplotlib.pyplot as plt
import os
import argparse

parser = argparse.ArgumentParser(description='Plotter for Sibilla')
parser.add_argument('path', action='store')
parser.add_argument('-m', '--model', type=int, action='store', default=1)
parser.add_argument('-n', '--customers', type=int, action='store', default=5)
parser.add_argument('-k', '--clerks', type=int, action='store', default=1)
parser.add_argument('-a', '--arrival-rate', type=int ,action='store', default=1)
parser.add_argument('-s', '--served-rate', type=int, action='store', default=1)
args = parser.parse_args()

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.5_0.1','ShopScenarionPM_10000_6_ClerkUtilisation_.data')
x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 10, args.served_rate),'ShopScenario_Utilisation.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 10$')
# ax.fill_between(x, y-se, y+se)

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 5, args.served_rate),'ShopScenario_Utilisation.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 5$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 2, args.served_rate),'ShopScenario_Utilisation.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 2$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 1, args.served_rate),'ShopScenario_Utilisation.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 1$')

ax.set_title('Clerk Utilisation ' + '$N = {0}, K = {1}, \beta = {2}$'.format(args.customers, args.clerks, args.served_rate))

plt.legend()
fig.savefig('Clerks_Utilisation.png')
plt.close(fig)

###########################################

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.5_0.1','ShopScenarionPM_10000_6_ClerkUtilisation_.data')
x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 10, args.served_rate),'ShopScenario_Served.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 10$')
# ax.fill_between(x, y-se, y+se)

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 5, args.served_rate),'ShopScenario_Served.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 5$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 2, args.served_rate),'ShopScenario_Served.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 2$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 1, args.served_rate),'ShopScenario_Served.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 1$')

ax.set_title('Customers Served ' + '$N = {0}, K = {1}, \beta = {2}$'.format(args.customers, args.clerks, args.served_rate))

plt.legend()
fig.savefig('Customers_Served.png')
plt.close(fig)

####################################

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.5_0.1','ShopScenarionPM_10000_6_ClerkUtilisation_.data')
x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 10, args.served_rate),'ShopScenario_Waiting.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 10$')
# ax.fill_between(x, y-se, y+se)

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 5, args.served_rate),'ShopScenario_Waiting.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 5$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 2, args.served_rate),'ShopScenario_Waiting.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 2$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 1, args.served_rate),'ShopScenario_Waiting.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 1$')

ax.set_title('Customers Waiting ' + '$N = {0}, K = {1}, \beta = {2}$'.format(args.customers, args.clerks, args.served_rate))

plt.legend()
fig.savefig('Customers_Waiting.png')
plt.close(fig)

###################################à

fig, ax = plt.subplots()

# os.path.join(os.getcwd(),'data','POCHI - POCHI','1','Data_5_1_0.5_0.1','ShopScenarionPM_10000_6_ClerkUtilisation_.data')
x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 10, args.served_rate),'ShopScenario_Outside.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 10$')
# ax.fill_between(x, y-se, y+se)

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 5, args.served_rate),'ShopScenario_Outside.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 5$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 2, args.served_rate),'ShopScenario_Outside.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 2$')

x, y, se = loadtxt(os.path.join(os.getcwd(),'data','ShopScenarioModel{0} - Data_N{1}_K{2}_A{3}_S{4}'.format(args.model, args.customers, args.clerks, 1, args.served_rate),'ShopScenario_Outside.data'), delimiter=';', unpack=True)
ax.plot(x,y, label=r'$\alpha = 1$')

ax.set_title('Customers Outside ' + '$N = {0}, K = {1}, \beta = {2}$'.format(args.customers, args.clerks, args.served_rate))

plt.legend()
fig.savefig('Customers_Outside.png')
plt.close(fig)
