## Helidon AOT Startup Optimization Benchmark
To make the benchmark close to business like use-case, the warmup/training is done
during docker build phase. That is a very practical way of doing AOT optimization,
simply by creating a Docker image with AOT pre-trained Helidon application.

## Tested optimizations
Each test optimization has its own docker file. Every optimization is tested twice, once for Helidon MP Hello World app and once for the Helidon SE version of the same.
- [Dockerfile.vanilla](./Dockerfile.vanilla) - Clean OpenJDK with no AOT warmup as a baseline
- [Dockerfile.leyden](./Dockerfile.leyden) - Leyden project [EA build](https://jdk.java.net/leyden/) with warmup/training during build phase
- [Dockerfile.nativeimage](./Dockerfile.nativeimage) - [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/) no warmup just AOT native image binary build in Docker build phase
- [Dockerfile.nativeimage-pgo](./Dockerfile.nativeimage-pgo) - [Profile-Guided Optimization(PGO)](https://www.graalvm.org/latest/reference-manual/native-image/guides/optimize-native-executable-with-pgo/) native image binary PGO build with warmup/training in Docker build phase
- [Dockerfile.crac](./Dockerfile.crac) - [Coordinated Restore and Checkpoint (CRaC)](https://crac.org/) CRaC snapshot created during Docker build phase 

## Measuring

### Build phase:
- **Warmup start** - measuring how long it takes to start the app for the training/warmup, instrumented or not
- **Warmup req/s** - measuring how many requests per seconds is application able to server during the warmup using [wrk tool](https://github.com/wg/wrk) `wrk -c 20 -t 10 -d 20s http://localhost:8080`
- **AOT/build sec** - measuring the build phase time in seconds, including warmup/training(default is 20 sec)
### Run phase:
- **5s run req/s** - first short requests per second measurement, 
by default 5 seconds long designed to show how optimized application is right after start, 
before JIT has the chance optimize much further. 
Default wrk params: `wrk -c 20 -t 10 -d 5s http://localhost:8080`
- **15s run req/s** - second longer requests per second measurement, 
meant to show if and how is application able to optimize further during the runtime. 
Default wrk params: `wrk -c 20 -t 10 -d 15s http://localhost:8080`



## Running benchmark

```bash
sudo dnf -y install git docker
git clone --single-branch --branch main https://github.com/helidon-io/helidon-labs.git
cd helidon-labs/benchmarks/startup
bash benchmark.sh
```

## Results
```markdown
Name                     |   AOT/build sec| Warmup start ms| Warmup req/s|  Startup ms| 5ss run req/s| 15ss run req/s
----------------------------------------------------------------------------------------------------------------------
Crac se                  |           16.06|             384|    369144.07|          20|     349244.56|     375627.50
Leyden se                |           28.99|             531|    366131.32|         126|     362764.13|     382723.54
Nativeimage se           |           50.70|                |             |           8|     374634.48|     378269.27
Nativeimage-pgo se       |          114.04|              38|    247108.15|           6|     438387.36|     429550.39
Vanilla se               |            5.73|                |             |         383|     347570.57|     379447.77
Crac mp                  |           17.62|            1620|     95163.17|          32|      92553.57|      99893.59
Leyden mp                |           82.71|            3374|     88586.15|         615|      73935.23|      85436.14
Nativeimage mp           |           96.47|                |             |          37|      77167.15|      79602.40
Nativeimage-pgo mp       |          239.37|             142|     27249.80|          31|      94230.85|      96838.39
Vanilla mp               |           11.64|                |             |        1597|      73078.18|     101808.88
----------------------------------------------------------------------------------------------------------------------
Results stored in /tmp/startup-benchmark-1741357244/results.csv
```

Test Environment:
```yaml
Shape:               VM.Standard3.Flex
OCPU count:          16
Network bandwidth (Gbps): 16
Memory (GB):         64
Local disk:          Block storage only
Image:               Oracle-Linux-8.10-2025.01.31-0
Architecture:        x86_64
CPU op-mode(s):      32-bit, 64-bit
Byte Order:          Little Endian
CPU(s):              32
On-line CPU(s) list: 0-31
Thread(s) per core:  2
Core(s) per socket:  16
Socket(s):           1
NUMA node(s):        1
Vendor ID:           GenuineIntel
CPU family:          6
Model:               106
Model name:          Intel(R) Xeon(R) Platinum 8358 CPU @ 2.60GHz
Stepping:            6
CPU MHz:             2593.994
BogoMIPS:            5187.98
Virtualization:      VT-x
Hypervisor vendor:   KVM
Virtualization type: full
L1d cache:           32K
L1i cache:           32K
L2 cache:            4096K
L3 cache:            16384K
NUMA node0 CPU(s):   0-31
```
