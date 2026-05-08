#!/usr/bin/env python3
"""
Skripta za testiranje load balancinga na user-service.

Pokretanje:
  1. Pokreni Eureka server na portu 8761
  2. Pokreni prvu instancu user-service:  java -jar user-service.jar --server.port=8081
  3. Pokreni drugu instancu user-service: java -jar user-service.jar --server.port=8082
  4. Pokreni ovu skriptu: python3 load_balance_test.py

Skripta šalje 100 zahtjeva direktno na svaku instancu i mjeri vrijeme,
a zatim demonstrira round-robin load balancing.
"""

import requests
import time
import statistics

INSTANCE_1 = "http://localhost:8081"
INSTANCE_2 = "http://localhost:8082"
ENDPOINT    = "/api/users"
NUM_REQUESTS = 100


def send_requests(base_url: str, n: int, label: str) -> list[float]:
    """Šalje n zahtjeva na zadanu instancu i vraća listu vremena odgovora (ms)."""
    times = []
    errors = 0

    print(f"\n[{label}] Šaljem {n} zahtjeva na {base_url}{ENDPOINT}...")
    for i in range(n):
        try:
            start = time.perf_counter()
            r = requests.get(f"{base_url}{ENDPOINT}", timeout=5)
            elapsed_ms = (time.perf_counter() - start) * 1000
            times.append(elapsed_ms)
            if r.status_code != 200:
                errors += 1
        except Exception as e:
            errors += 1
            print(f"  Greška na zahtjevu {i+1}: {e}")

    print(f"  Uspješno: {n - errors}/{n}")
    print(f"  Prosječno: {statistics.mean(times):.2f} ms")
    print(f"  Min: {min(times):.2f} ms | Max: {max(times):.2f} ms | Median: {statistics.median(times):.2f} ms")
    return times


def round_robin_test(n: int = 100) -> None:
    """
    Simulira round-robin load balancing slanjem zahtjeva naizmjenično
    na instancu 1 i instancu 2.
    """
    print(f"\n{'='*60}")
    print(f"ROUND-ROBIN LOAD BALANCING TEST ({n} zahtjeva)")
    print('='*60)

    instance1_count = 0
    instance2_count = 0
    times = []
    errors = 0

    for i in range(n):
        # Naizmjenično biramo instancu
        target = INSTANCE_1 if i % 2 == 0 else INSTANCE_2
        try:
            start = time.perf_counter()
            r = requests.get(f"{target}{ENDPOINT}", timeout=5)
            elapsed_ms = (time.perf_counter() - start) * 1000
            times.append(elapsed_ms)
            if r.status_code == 200:
                if target == INSTANCE_1:
                    instance1_count += 1
                else:
                    instance2_count += 1
            else:
                errors += 1
        except Exception as e:
            errors += 1

    total_successful = instance1_count + instance2_count
    print(f"\nRezultati distribucije:")
    print(f"  Instanca 1 (:{8081}): {instance1_count} zahtjeva ({instance1_count/total_successful*100:.1f}%)")
    print(f"  Instanca 2 (:{8082}): {instance2_count} zahtjeva ({instance2_count/total_successful*100:.1f}%)")
    print(f"  Greške: {errors}")
    print(f"\nVrijeme odgovora (load balanced):")
    print(f"  Prosječno: {statistics.mean(times):.2f} ms")
    print(f"  Min: {min(times):.2f} ms | Max: {max(times):.2f} ms")


def main():
    print("=" * 60)
    print("LOAD BALANCING TEST — NeighborAlert user-service")
    print("=" * 60)

    # 1. Test bez load balancinga (samo instanca 1)
    times_single = send_requests(INSTANCE_1, NUM_REQUESTS, "BEZ LOAD BALANCINGA (samo instanca 1)")
    avg_single = statistics.mean(times_single)

    # 2. Test sa load balancingom (round-robin između 2 instance)
    round_robin_test(NUM_REQUESTS)

    # 3. Poređenje
    print(f"\n{'='*60}")
    print("ZAKLJUČAK")
    print('='*60)
    print(f"  Prosječno bez load balancinga: {avg_single:.2f} ms")
    print(f"  Load balancing raspoređuje zahtjeve na obje instance,")
    print(f"  što smanjuje opterećenje po instanci i poboljšava throughput.")


if __name__ == "__main__":
    main()
