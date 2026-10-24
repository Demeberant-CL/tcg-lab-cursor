import React, { useEffect, useState } from "react";
import { Navbar } from "@/components/Navbar";
import { BottomNav, type TabType } from "@/components/BottomNav";
import { QrModal } from "@/components/QrModal";
import { HomeScreen } from "@/screens/HomeScreen";
import { CollectionScreen } from "@/screens/CollectionScreen";
import { SetDetailScreen } from "@/screens/SetDetailScreen";
import { DecksScreen } from "@/screens/DecksScreen";
import { DeckEditorScreen } from "@/screens/DeckEditorScreen";
import { StrategyScreen } from "@/screens/StrategyScreen";
import { SettingsScreen } from "@/screens/SettingsScreen";
import type { CardRow, Deck } from "@/lib/folio/types";
import { repository } from "@/lib/repository";

export const App: React.FC = () => {
  const [, setTick] = useState(0);

  // Navigation state
  const [currentTab, setCurrentTab] = useState<TabType>("home");
  const [selectedSetCode, setSelectedSetCode] = useState<string | null>(null);
  const [editingDeck, setEditingDeck] = useState<Deck | null | "new">(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);
  const [qrDeck, setQrDeck] = useState<Deck | null>(null);

  // Subscribe to repository updates
  useEffect(() => {
    const unsubscribe = repository.subscribe(() => {
      setTick((t) => t + 1);
    });
    return unsubscribe;
  }, []);

  // Compute page title
  const getHeaderInfo = () => {
    if (isSettingsOpen) return { title: "Ajustes", subtitle: undefined };
    if (editingDeck !== null) return { title: "Editor de mazo", subtitle: undefined };
    if (selectedSetCode) return { title: "Expansión", subtitle: selectedSetCode };

    switch (currentTab) {
      case "home":
        return { title: "Centro de juego", subtitle: undefined };
      case "collection":
        return { title: "Colección", subtitle: undefined };
      case "decks":
        return { title: "Mazos", subtitle: undefined };
      case "strategy":
        return { title: "TCG LAB AI", subtitle: undefined };
      default:
        return { title: "TCG LAB", subtitle: undefined };
    }
  };

  const headerInfo = getHeaderInfo();

  // Handling adding a card from set detail to the currently open deck or a new deck
  const handleAddToDeck = (card: CardRow) => {
    const decks = repository.getDecks();
    const targetDeck = decks[0];
    if (targetDeck) {
      const existing = targetDeck.cards.find((c) => c.set === card.set && c.number === card.number);
      if (existing) {
        existing.count = Math.min(2, existing.count + 1);
      } else {
        targetDeck.cards.push({ set: card.set, number: card.number, count: 1 });
      }
      repository.saveDeck(targetDeck);
      setEditingDeck(targetDeck);
    } else {
      const newDeck: Deck = {
        id: `deck-${Date.now()}`,
        name: "Nuevo mazo",
        energies: [card.trainer ? 2 : 1],
        cards: [{ set: card.set, number: card.number, count: 1 }],
        updatedAt: Date.now(),
      };
      repository.saveDeck(newDeck);
      setEditingDeck(newDeck);
    }
    setSelectedSetCode(null);
  };

  return (
    <div className="min-h-screen bg-[#F3F0E8] dark:bg-[#101820] text-[#172536] dark:text-[#F0EEE7] flex flex-col">
      {/* Top App Header */}
      <Navbar
        title={headerInfo.title}
        subtitle={headerInfo.subtitle}
        onOpenSettings={() => setIsSettingsOpen(true)}
      />

      {/* Main Content Viewport */}
      <main className="flex-1 max-w-xl w-full mx-auto px-4 py-3">
        {isSettingsOpen ? (
          <SettingsScreen onBack={() => setIsSettingsOpen(false)} />
        ) : editingDeck !== null ? (
          <DeckEditorScreen
            initialDeck={editingDeck === "new" ? null : editingDeck}
            onBack={() => setEditingDeck(null)}
          />
        ) : selectedSetCode !== null ? (
          <SetDetailScreen
            setCode={selectedSetCode}
            onBack={() => setSelectedSetCode(null)}
            onAddToDeck={handleAddToDeck}
          />
        ) : currentTab === "home" ? (
          <HomeScreen
            onOpenCollection={() => setCurrentTab("collection")}
            onOpenDecks={() => setCurrentTab("decks")}
            onOpenStrategy={() => setCurrentTab("strategy")}
            onOpenSet={(code) => setSelectedSetCode(code)}
          />
        ) : currentTab === "collection" ? (
          <CollectionScreen onSelectSet={(code) => setSelectedSetCode(code)} />
        ) : currentTab === "decks" ? (
          <DecksScreen
            onOpenDeck={(deck) => setEditingDeck(deck)}
            onNewDeck={() => setEditingDeck("new")}
            onOpenStrategy={() => setCurrentTab("strategy")}
            onOpenQr={(deck) => setQrDeck(deck)}
          />
        ) : (
          <StrategyScreen onOpenDeck={(deck) => setEditingDeck(deck)} />
        )}
      </main>

      {/* Bottom Nav: Only visible on top-level tabs */}
      {!isSettingsOpen && editingDeck === null && selectedSetCode === null && (
        <BottomNav
          currentTab={currentTab}
          onSelectTab={(tab) => {
            setCurrentTab(tab);
            setSelectedSetCode(null);
            setEditingDeck(null);
            setIsSettingsOpen(false);
          }}
        />
      )}

      {/* QR Code Modal for viewing from decks screen */}
      {qrDeck && (
        <QrModal deck={qrDeck} onClose={() => setQrDeck(null)} />
      )}
    </div>
  );
};
