#!/usr/bin/env python3

import requests
import time
import statistics
from datetime import datetime

# ── Konfiguracija ──────────────────────────────────────────────────────────────
INTERACTION_SERVICE = "http://localhost:8083"
# Endpoint koji interno koristi @LoadBalanced RestTemplate za poziv user-service
LB_ENDPOINT = "/api/lb-test/user/1"

# Za test bez load balancinga — direktni poziv na instancu 1
DIRECT_INSTANCE_1 = "http://localhost:8081"
DIRECT_ENDPOINT = "/api/users/1"

NUM_REQUESTS = 100
# ───────────────────────────────────────────────────────────────────────────────


def send_requests(base_url: str, endpoint: str, n: int, label: str) -> list:
    """
    Šalje n zahtjeva na zadani URL i vraća listu vremena odgovora u ms.
    """
    times = []
    errors = 0
    url = f"{base_url}{endpoint}"

    print(f"\n{'─'*60}")
    print(f"  {label}")
    print(f"  URL: {url}")
    print(f"  Broj zahtjeva: {n}")
    print(f"{'─'*60}")

    for i in range(1, n + 1):
        try:
            start = time.perf_counter()
            r = requests.get(url, timeout=5)
            elapsed_ms = (time.perf_counter() - start) * 1000
            times.append(elapsed_ms)
            if r.status_code not in (200, 404):
                errors += 1
                print(f"  [!] Zahtjev {i}: HTTP {r.status_code}")
        except requests.exceptions.ConnectionError:
            errors += 1
            if i == 1:
                print(f"  [GRESKA] Ne mogu se spojiti na {url}")
                print(f"           Provjeri da li su servisi pokrenuti.")
            break
        except Exception as e:
            errors += 1
            print(f"  [GRESKA] Zahtjev {i}: {e}")

    if not times:
        print("  Nema uspjesnih zahtjeva.")
        return []

    print(f"\n  Rezultati:")
    print(f"    Uspjesno:  {len(times)}/{n}")
    print(f"    Greske:    {errors}")
    print(f"    Prosjek:   {statistics.mean(times):.2f} ms")
    print(f"    Median:    {statistics.median(times):.2f} ms")
    print(f"    Min:       {min(times):.2f} ms")
    print(f"    Max:       {max(times):.2f} ms")
    if len(times) > 1:
        print(f"    Std. dev.: {statistics.stdev(times):.2f} ms")
    return times


def check_service(url: str, label: str) -> bool:
    """Provjeri da li je servis dostupan."""
    try:
        r = requests.get(f"{url}/actuator/health", timeout=3)
        if r.status_code == 200:
            print(f"  OK: {label} ({url}) — dostupan")
            return True
    except Exception:
        pass
    print(f"  NEDOSTUPAN: {label} ({url})")
    return False


def main():
    print("=" * 60)
    print("  LOAD BALANCING TEST — NeighborAlert")
    print(f"  {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # ── Provjera dostupnosti servisa ──────────────────────────────
    print("\nProvjera servisa:")
    interaction_ok = check_service(INTERACTION_SERVICE, "interaction-service")
    check_service(DIRECT_INSTANCE_1, "user-service (instanca 1, :8081)")

    if not interaction_ok:
        print("\n[STOP] interaction-service nije dostupan. Pokreni ga na portu 8083.")
        return

    # ─────────────────────────────────────────────────────────────
    # TEST 1: Bez load balancinga
    # Direktni pozivi na user-service instancu 1 (hardkodirani port)
    # ─────────────────────────────────────────────────────────────
    times_direct = send_requests(
        DIRECT_INSTANCE_1,
        DIRECT_ENDPOINT,
        NUM_REQUESTS,
        "TEST 1: BEZ LOAD BALANCINGA\n  Direktni pozivi na user-service :8081 (hardkodirani port)"
    )

    # ─────────────────────────────────────────────────────────────
    # TEST 2: Sa load balancingom (@LoadBalanced RestTemplate)
    #
    # Svi zahtjevi idu na interaction-service koji INTERNO poziva:
    #   http://user-service/api/users/1
    # Spring Cloud LoadBalancer automatski bira instancu (round-robin).
    # Skripta ne zna i ne bira na kojoj instanci zavrsava zahtjev.
    # ─────────────────────────────────────────────────────────────
    times_lb = send_requests(
        INTERACTION_SERVICE,
        LB_ENDPOINT,
        NUM_REQUESTS,
        "TEST 2: SA LOAD BALANCINGOM (@LoadBalanced RestTemplate)\n"
        "  Svi zahtjevi -> interaction-service -> @LoadBalanced RestTemplate\n"
        "  -> Spring Cloud LoadBalancer bira user-service instancu automatski"
    )

    # ─────────────────────────────────────────────────────────────
    # Poredjenje rezultata
    # ─────────────────────────────────────────────────────────────
    print(f"\n{'='*60}")
    print("  POREDJENJE REZULTATA")
    print(f"{'='*60}")

    if times_direct and times_lb:
        avg_direct = statistics.mean(times_direct)
        avg_lb = statistics.mean(times_lb)
        diff = avg_lb - avg_direct
        diff_pct = (diff / avg_direct) * 100

        print(f"""
  Bez load balancinga (direktno na :8081):
    Prosjecno:  {avg_direct:.2f} ms
    Median:     {statistics.median(times_direct):.2f} ms

  Sa load balancingom (interaction-service -> LoadBalancer):
    Prosjecno:  {avg_lb:.2f} ms
    Median:     {statistics.median(times_lb):.2f} ms

  Razlika:      {diff:+.2f} ms ({diff_pct:+.1f}%)
  Napomena: LB moze biti sporiji jer ide kroz dodatni hop (interaction-service),
  ali rasporeduje opterecenje na vise instanci.
""")

    print("  RASPODJELA ZAHTJEVA PO INSTANCAMA:")
    
    print()
    print(f"  Ocekivana raspodjela s 2 instance (round-robin):")
    print(f"    user-service:8081 — ~{NUM_REQUESTS // 2} zahtjeva (~50%)")
    print(f"    user-service:8085 — ~{NUM_REQUESTS // 2} zahtjeva (~50%)")
    print()

    print("=" * 60)


if __name__ == "__main__":
    main()
