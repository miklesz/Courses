class KontoBankowe:
    def __init__(self, nazwa, stan=0):
        self.nazwa = nazwa
        self.stan = stan

    def info(self):
        print("nazwa:", self.nazwa)
        print("stan:", self.stan)

    def wyplac(self, ilosc):
        self.stan -= ilosc

    def wplac(self, ilosc):
        self.stan += ilosc
